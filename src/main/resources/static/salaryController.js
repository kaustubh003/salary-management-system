var app = angular.module('salaryManagementApp');

function salaryErrorMessage(error, fallback) {
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

function localDateFromString(value) {
    if (!value) {
        return null;
    }
    var parts = value.split('-');
    return new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
}

function localDateString(value) {
    if (!value) {
        return null;
    }
    var year = value.getFullYear();
    var month = String(value.getMonth() + 1).padStart(2, '0');
    var day = String(value.getDate()).padStart(2, '0');
    return year + '-' + month + '-' + day;
}

function serializeSalaryForm(salary) {
    return {
        effectiveFrom: localDateString(salary.effectiveFrom),
        effectiveTill: localDateString(salary.effectiveTill),
        currency: salary.currency,
        basicSalary: salary.basicSalary,
        hra: salary.hra,
        da: salary.da,
        tax: salary.tax,
        epf: salary.epf
    };
}

function setSalaryDates(salary) {
    salary.effectiveFrom = localDateFromString(salary.effectiveFrom);
    salary.effectiveTill = localDateFromString(salary.effectiveTill);
    return salary;
}

function calculateSalary(salary) {
    var basic = Number(salary.basicSalary) || 0;
    var hra = Number(salary.hra) || 0;
    var da = Number(salary.da) || 0;
    var tax = Number(salary.tax) || 0;
    var epf = Number(salary.epf) || 0;
    salary.grossSalary = basic + hra + da;
    salary.netSalary = salary.grossSalary - tax - epf;
}

app.controller('SalaryManagementController', function ($scope, $state, $q, $window, SalaryService, SalarySelection) {
    $scope.employeeCode = SalarySelection.employeeCode || '';
    $scope.employee = null;
    $scope.salaries = [];
    $scope.selected = SalarySelection.selected;
    $scope.selectedCount = 0;
    $scope.loading = false;
    $scope.deleting = false;
    $scope.error = '';
    $scope.notice = '';
    $scope.hasSearched = false;
    if ($scope.employeeCode) {
        $scope.hasSearched = true;
    }

    $scope.search = function () {
        var employeeCode = ($scope.employeeCode || '').trim();
        if (!employeeCode) {
            $scope.error = 'Enter an employee code to search.';
            return;
        }

        $scope.loading = true;
        SalarySelection.employeeCode = employeeCode;
        $scope.error = '';
        $scope.notice = '';
        $scope.employee = null;
        $scope.salaries = [];
        $scope.hasSearched = true;

        SalaryService.getEmployee(employeeCode).then(function (response) {
            $scope.employee = response.data;
            return SalaryService.getForEmployee(employeeCode);
        }).then(function (response) {
            $scope.salaries = response.data.slice().sort(function (left, right) {
                return right.effectiveFrom.localeCompare(left.effectiveFrom);
            });
            angular.forEach($scope.salaries, function (salary) {
                if (!$scope.selected.hasOwnProperty(salary.id)) {
                    $scope.selected[salary.id] = false;
                }
            });
            updateSelectionCount();
        }).catch(function (error) {
            $scope.employee = null;
            $scope.salaries = [];
            $scope.error = salaryErrorMessage(error, 'Unable to load salary records. Please try again.');
        }).finally(function () {
            $scope.loading = false;
        });
    };

    $scope.updateSelectionCount = updateSelectionCount;
    $scope.toggleAll = function () {
        var selectAll = !$scope.isAllSelected();
        angular.forEach($scope.salaries, function (salary) {
            $scope.selected[salary.id] = selectAll;
        });
        updateSelectionCount();
    };
    $scope.isAllSelected = function () {
        return $scope.salaries.length > 0 && $scope.salaries.every(function (salary) {
            return $scope.selected[salary.id];
        });
    };

    $scope.openSalary = function (salary) {
        $state.go('dashboard.salaryManagement.details', {
            salaryId: salary.id,
            employeeCode: $scope.employee.employeeCode
        });
    };

    $scope.addSalary = function () {
        SalarySelection.employeeCode = $scope.employee.employeeCode;
        $state.go('dashboard.salaryManagement.add', { employeeCode: $scope.employee.employeeCode });
    };

    $scope.deleteSelected = function () {
        var ids = $scope.salaries.filter(function (salary) {
            return $scope.selected[salary.id];
        }).map(function (salary) {
            return salary.id;
        });
        if (!ids.length || !$window.confirm('Delete ' + ids.length + ' selected salary record(s)? This action cannot be undone.')) {
            return;
        }

        $scope.deleting = true;
        $scope.error = '';
        $scope.notice = '';
        var deleted = [];
        var failed = [];

        function deleteNext(index) {
            if (index >= ids.length) {
                return $q.when();
            }
            return SalaryService.delete(ids[index]).then(function () {
                deleted.push(ids[index]);
                delete $scope.selected[ids[index]];
                return deleteNext(index + 1);
            }, function () {
                failed.push(ids[index]);
                return deleteNext(index + 1);
            });
        }

        deleteNext(0).then(function () {
            updateSelectionCount();
            if (failed.length) {
                $scope.error = 'Could not delete salary record(s): ' + failed.join(', ') + '. Please try again.';
            } else {
                $scope.notice = deleted.length + ' salary record(s) deleted.';
            }
            if (deleted.length) {
                return $scope.search();
            }
        }).finally(function () {
            $scope.deleting = false;
        });
    };

    function updateSelectionCount() {
        $scope.selectedCount = $scope.salaries.filter(function (salary) {
            return $scope.selected[salary.id];
        }).length;
    }

    if ($scope.employeeCode) {
        $scope.search();
    }
});

app.controller('SalaryDetailsController', function ($scope, $state, $stateParams, $window, SalaryService, SalarySelection) {
    $scope.loading = true;
    $scope.saving = false;
    $scope.error = '';
    $scope.notice = '';
    var tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    $scope.minimumEffectiveFrom = localDateString(tomorrow);
    $scope.effectiveFromError = '';

    SalaryService.get($stateParams.salaryId).then(function (response) {
        $scope.salary = setSalaryDates(response.data);
        $scope.employeeCode = $stateParams.employeeCode || SalarySelection.employeeCode || '';
        $scope.initialEffectiveFrom = localDateString($scope.salary.effectiveFrom);
        $scope.previousEffectiveTill = $scope.salary.effectiveTill;
        calculateSalary($scope.salary);
    }, function (error) {
        $scope.error = salaryErrorMessage(error, 'Unable to load salary details. Please try again.');
    }).finally(function () {
        $scope.loading = false;
    });

    $scope.updateCalculatedAmounts = function () {
        calculateSalary($scope.salary);
    };

    $scope.effectiveFromChanged = function () {
        var effectiveFrom = localDateString($scope.salary && $scope.salary.effectiveFrom);
        $scope.effectiveFromError = effectiveFrom !== $scope.initialEffectiveFrom &&
            effectiveFrom < $scope.minimumEffectiveFrom
            ? 'A changed Effective From date must be in the future.'
            : '';
    };

    $scope.effectiveTillChanged = function () {
        if (!$scope.salary || !$scope.salary.effectiveTill) {
            return;
        }
        if (!$window.confirm('This salary will no longer be active after ' + localDateString($scope.salary.effectiveTill) + '. Continue?')) {
            $scope.salary.effectiveTill = $scope.previousEffectiveTill || null;
            return;
        }
        $scope.previousEffectiveTill = $scope.salary.effectiveTill;
    };

    $scope.save = function (form) {
        if (!form || form.$invalid || $scope.effectiveFromError) {
            return;
        }
        $scope.saving = true;
        $scope.error = '';
        $scope.notice = '';
        SalaryService.update($scope.salary.id, serializeSalaryForm($scope.salary)).then(function () {
            $scope.notice = 'Salary details have been saved.';
        }, function (error) {
            $scope.error = salaryErrorMessage(error, 'Unable to save salary details. Please try again.');
        }).finally(function () {
            $scope.saving = false;
        });
    };

    $scope.cancel = function () {
        $state.go('dashboard.salaryManagement.list', {}, { reload: true });
    };
});

app.controller('AddSalaryController', function ($scope, $state, $stateParams, SalaryService, SalarySelection) {
    $scope.employeeCode = $stateParams.employeeCode || SalarySelection.employeeCode || '';
    $scope.salary = { currency: 'INR' };
    $scope.saving = false;
    $scope.error = '';
    $scope.notice = '';
    calculateSalary($scope.salary);

    $scope.updateCalculatedAmounts = function () {
        calculateSalary($scope.salary);
    };

    $scope.save = function (form) {
        if (!form || form.$invalid || !$scope.employeeCode) {
            return;
        }
        $scope.saving = true;
        $scope.error = '';
        SalarySelection.employeeCode = $scope.employeeCode;
        SalaryService.create($scope.employeeCode, serializeSalaryForm($scope.salary)).then(function () {
            $scope.notice = 'Salary details have been saved.';
            return $state.go('dashboard.salaryManagement.list', {}, { reload: true });
        }, function (error) {
            $scope.error = salaryErrorMessage(error, 'Unable to save salary details. Please try again.');
        }).finally(function () {
            $scope.saving = false;
        });
    };

    $scope.cancel = function () {
        $state.go('dashboard.salaryManagement.list');
    };
});
