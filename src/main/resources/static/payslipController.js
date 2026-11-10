var app = angular.module('salaryManagementApp');

function payslipErrorMessage(error, fallback) {
    if (error && error.status === 404) {
        return 'This payslip has not been generated yet. Generate it before downloading.';
    }
    if (error && error.data && error.data.errorMessage) {
        return error.data.errorMessage;
    }
    if (error && error.data && angular.isObject(error.data) && !(error.data instanceof Blob)) {
        return Object.keys(error.data).map(function (field) {
            return error.data[field];
        }).join(' ');
    }
    return fallback;
}

app.controller('PayslipManagementController', function ($scope, $q, PayslipService, PayslipSelection) {
    $scope.employeeCode = PayslipSelection.employeeCode || '';
    $scope.payments = [];
    $scope.selected = PayslipSelection.selected;
    $scope.selectedCount = 0;
    $scope.page = 0;
    $scope.pagination = { pageSize: 20 };
    $scope.pageSizes = [10, 20, 50, 100];
    $scope.totalPages = 0;
    $scope.totalElements = 0;
    $scope.loading = false;
    $scope.generating = false;
    $scope.downloading = {};
    $scope.error = '';
    $scope.notice = '';
    $scope.employeeFound = false;

    $scope.search = function () {
        var employeeCode = ($scope.employeeCode || '').trim();
        if (!employeeCode) {
            $scope.error = 'Enter an employee code to search.';
            return;
        }
        if (employeeCode !== PayslipSelection.employeeCode) {
            PayslipSelection.selected = {};
            $scope.selected = PayslipSelection.selected;
        }
        $scope.employeeCode = employeeCode;
        PayslipSelection.employeeCode = employeeCode;
        $scope.page = 0;
        $scope.error = '';
        $scope.notice = '';
        $scope.payments = [];
        $scope.employeeFound = false;
        $scope.loadPage();
    };

    $scope.loadPage = function () {
        if (!$scope.employeeCode) {
            return;
        }
        $scope.loading = true;
        $scope.error = '';
        PayslipService.getPayments($scope.employeeCode, $scope.page, $scope.pagination.pageSize)
            .then(function (response) {
                var result = response.data;
                $scope.payments = result.content || [];
                $scope.totalPages = result.totalPages || 0;
                $scope.totalElements = result.totalElements || 0;
                $scope.employeeFound = true;
                angular.forEach($scope.payments, function (payment) {
                    if (!$scope.selected.hasOwnProperty(payment.paymentID)) {
                        $scope.selected[payment.paymentID] = false;
                    }
                });
                updateSelectionCount();
                if ($scope.totalPages > 0 && $scope.page >= $scope.totalPages) {
                    $scope.page = $scope.totalPages - 1;
                    return $scope.loadPage();
                }
            })
            .catch(function (error) {
                $scope.payments = [];
                $scope.totalPages = 0;
                $scope.totalElements = 0;
                $scope.employeeFound = false;
                $scope.error = payslipErrorMessage(error, 'Unable to load payment records. Please try again.');
            })
            .finally(function () {
                $scope.loading = false;
            });
    };

    $scope.changePageSize = function () {
        $scope.page = 0;
        $scope.loadPage();
    };

    $scope.previousPage = function () {
        if ($scope.page > 0 && !$scope.loading) {
            $scope.page -= 1;
            $scope.loadPage();
        }
    };

    $scope.nextPage = function () {
        if ($scope.page + 1 < $scope.totalPages && !$scope.loading) {
            $scope.page += 1;
            $scope.loadPage();
        }
    };

    $scope.updateSelectionCount = updateSelectionCount;
    $scope.isAllSelected = function () {
        return $scope.payments.length > 0 && $scope.payments.every(function (payment) {
            return $scope.selected[payment.paymentID];
        });
    };
    $scope.toggleAll = function () {
        var selectAll = !$scope.isAllSelected();
        angular.forEach($scope.payments, function (payment) {
            $scope.selected[payment.paymentID] = selectAll;
        });
        updateSelectionCount();
    };

    $scope.generateSelected = function () {
        var paymentIds = Object.keys($scope.selected).filter(function (paymentId) {
            return $scope.selected[paymentId];
        });
        if (!paymentIds.length || $scope.generating) {
            return;
        }

        $scope.generating = true;
        $scope.error = '';
        $scope.notice = '';
        var generated = 0;
        var failed = [];

        function generateNext(index) {
            if (index >= paymentIds.length) {
                return $q.when();
            }
            return PayslipService.generate(paymentIds[index]).then(function () {
                generated += 1;
            }, function () {
                failed.push(index + 1);
            }).then(function () {
                return generateNext(index + 1);
            });
        }

        generateNext(0).then(function () {
            $scope.notice = generated + ' payslip(s) generated.';
            if (failed.length) {
                $scope.error = 'Could not generate payslip(s) for ' + failed.length
                    + ' selected payment(s). Please try again.';
            }
        }).finally(function () {
            $scope.generating = false;
        });
    };

    $scope.generateAll = function () {
        if ($scope.generating) {
            return;
        }
        $scope.generating = true;
        $scope.error = '';
        $scope.notice = '';
        PayslipService.generateAll().then(function (response) {
            var result = response.data;
            $scope.notice = result.generated + ' payslip(s) generated; '
                + result.skipped + ' already existed.';
        }).catch(function (error) {
            $scope.error = payslipErrorMessage(error, 'Unable to generate all payslips. Please try again.');
        }).finally(function () {
            $scope.generating = false;
        });
    };

    $scope.download = function (payment) {
        var paymentId = payment.paymentID;
        if ($scope.downloading[paymentId]) {
            return;
        }
        $scope.error = '';
        $scope.notice = '';
        $scope.downloading[paymentId] = true;
        PayslipService.download(paymentId).then(function (response) {
            var blob = response.data instanceof Blob
                ? response.data
                : new Blob([response.data], { type: 'application/pdf' });
            var url = window.URL.createObjectURL(blob);
            var link = document.createElement('a');
            link.href = url;
            link.download = 'payslip-' + payment.periodStart + '.pdf';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        }).catch(function (error) {
            $scope.error = payslipErrorMessage(error, 'Unable to download the payslip. Please try again.');
        }).finally(function () {
            delete $scope.downloading[paymentId];
        });
    };

    function updateSelectionCount() {
        $scope.selectedCount = Object.keys($scope.selected).filter(function (paymentId) {
            return $scope.selected[paymentId];
        }).length;
    }

    if ($scope.employeeCode) {
        $scope.loadPage();
    }
});
