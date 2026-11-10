var app = angular.module('salaryManagementApp');

function paymentErrorMessage(error, fallback) {
    if (error && error.data && error.data.errorMessage) {
        return error.data.errorMessage;
    }
    if (error && error.data && angular.isObject(error.data)) {
        return Object.keys(error.data).map(function (field) {
            return error.data[field];
        }).join(' ');
    }
    return fallback;
}

function paymentLocalDateString(value) {
    if (!value) {
        return null;
    }
    return value.getFullYear() + '-' +
        String(value.getMonth() + 1).padStart(2, '0') + '-' +
        String(value.getDate()).padStart(2, '0');
}

function parsePaymentLocalDate(value) {
    if (!value) {
        return null;
    }
    var parts = value.split('-');
    return new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
}

app.controller('PaymentManagementController', function ($scope, $state, PaymentService, PaymentSelection) {
    $scope.employeeCode = PaymentSelection.employeeCode || '';
    $scope.payments = [];
    $scope.page = 0;
    $scope.pagination = { pageSize: 20 };
    $scope.pageSizes = [10, 20, 50, 100];
    $scope.totalPages = 0;
    $scope.totalElements = 0;
    $scope.loading = false;
    $scope.hasSearched = false;
    $scope.error = '';
    $scope.employeeFound = false;

    $scope.search = function () {
        var employeeCode = ($scope.employeeCode || '').trim();
        if (!employeeCode) {
            $scope.error = 'Enter an employee code to search.';
            return;
        }
        PaymentSelection.employeeCode = employeeCode;
        $scope.employeeCode = employeeCode;
        $scope.page = 0;
        $scope.hasSearched = true;
        $scope.employeeFound = false;
        $scope.payments = [];
        $scope.loadPage();
    };

    $scope.loadPage = function () {
        if (!$scope.employeeCode) {
            return;
        }
        $scope.loading = true;
        $scope.error = '';
        PaymentService.getPayments($scope.employeeCode, $scope.page, $scope.pagination.pageSize).then(function (response) {
            var result = response.data;
            $scope.payments = result.content || [];
            $scope.totalPages = result.totalPages || 0;
            $scope.totalElements = result.totalElements || 0;
            $scope.employeeFound = true;
            if ($scope.totalPages > 0 && $scope.page >= $scope.totalPages) {
                $scope.page = $scope.totalPages - 1;
                return $scope.loadPage();
            }
        }).catch(function (error) {
            $scope.payments = [];
            $scope.totalPages = 0;
            $scope.totalElements = 0;
            $scope.error = paymentErrorMessage(error, 'Unable to load payment records. Please try again.');
        }).finally(function () {
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

    $scope.addPayment = function () {
        $state.go('dashboard.payments.add', { employeeCode: $scope.employeeCode });
    };

    if ($scope.employeeCode) {
        $scope.hasSearched = true;
        $scope.loadPage();
    }
});

app.controller('AddPaymentController', function ($scope, $state, $stateParams, $q, PaymentService, PaymentSelection) {
    $scope.employeeCode = $stateParams.employeeCode || PaymentSelection.employeeCode || '';
    PaymentSelection.employeeCode = $scope.employeeCode;
    $scope.payment = { periodStart: null, periodEnd: null };
    $scope.loading = true;
    $scope.saving = false;
    $scope.error = '';
    $scope.salaries = [];
    $scope.leaves = [];
    $scope.availableLeaves = [];
    $scope.salary = null;
    $scope.unpaidLeaveDays = 0;
    $scope.leaveDeduction = 0;

    if (!$scope.employeeCode) {
        $scope.loading = false;
        $scope.error = 'Search for an employee before adding a payment.';
    } else {
        $q.all([
            PaymentService.getSalaries($scope.employeeCode),
            PaymentService.getLeaves($scope.employeeCode),
            PaymentService.getAvailableLeaves($scope.employeeCode)
        ]).then(function (responses) {
            $scope.salaries = responses[0].data;
            $scope.leaves = responses[1].data;
            $scope.availableLeaves = responses[2].data;
            if (!$scope.salaries.length) {
                $scope.error = 'This employee has no salary records. Add a salary before recording a payment.';
            }
        }).catch(function (error) {
            $scope.error = paymentErrorMessage(error, 'Unable to load payment details. Please try again.');
        }).finally(function () {
            $scope.loading = false;
        });
    }

    $scope.updatePreview = function () {
        var start = $scope.payment.periodStart;
        var end = $scope.payment.periodEnd;
        $scope.salary = null;
        $scope.unpaidLeaveDays = 0;
        $scope.leaveDeduction = 0;

        if (!start || !end || end < start) {
            return;
        }

        $scope.salary = $scope.salaries.find(function (salary) {
            var effectiveFrom = parsePaymentLocalDate(salary.effectiveFrom);
            var effectiveTill = salary.effectiveTill ? parsePaymentLocalDate(salary.effectiveTill) : null;
            return start >= effectiveFrom && (!effectiveTill || end <= effectiveTill);
        }) || null;

        if (!$scope.salary) {
            return;
        }

        var startString = paymentLocalDateString(start);
        var endString = paymentLocalDateString(end);
        var unpaidLeaveDays = $scope.leaves.filter(function (leave) {
            return leave.leaveType === 'UNPAID' &&
                leave.leaveDate >= startString && leave.leaveDate <= endString;
        }).length;
        $scope.availableLeaves.forEach(function (balance) {
            if (balance.availableLeaves < 0) {
                unpaidLeaveDays += Math.abs(balance.availableLeaves);
            }
        });
        $scope.unpaidLeaveDays = unpaidLeaveDays;

        var weekdays = 0;
        var date = new Date(start.getFullYear(), start.getMonth(), start.getDate());
        var finalDate = new Date(end.getFullYear(), end.getMonth(), end.getDate());
        while (date <= finalDate) {
            var weekday = date.getDay();
            if (weekday !== 0 && weekday !== 6) {
                weekdays += 1;
            }
            date.setDate(date.getDate() + 1);
        }

        if (unpaidLeaveDays > 0 && weekdays > 0) {
            var grossSalary = Number($scope.salary.basicSalary) +
                Number($scope.salary.hra) + Number($scope.salary.da);
            var netSalary = Number($scope.salary.netSalary);
            var perDayDeduction = Math.round((grossSalary / weekdays) * 100) / 100;
            $scope.leaveDeduction = Math.min(perDayDeduction * unpaidLeaveDays, Math.max(netSalary, 0));
        }
    };

    $scope.save = function (form) {
        if (!form || form.$invalid || !$scope.salary) {
            return;
        }
        $scope.saving = true;
        $scope.error = '';
        PaymentService.create({
            salaryId: $scope.salary.id,
            periodStart: paymentLocalDateString($scope.payment.periodStart),
            periodEnd: paymentLocalDateString($scope.payment.periodEnd)
        }).then(function () {
            return $state.go('dashboard.payments.list', {}, { reload: true });
        }).catch(function (error) {
            $scope.error = paymentErrorMessage(error, 'Unable to record the payment. Please try again.');
        }).finally(function () {
            $scope.saving = false;
        });
    };

    $scope.cancel = function () {
        $state.go('dashboard.payments.list');
    };
});
