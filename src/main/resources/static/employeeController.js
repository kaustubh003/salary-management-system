var app = angular.module('salaryManagementApp');

function employeeErrorMessage(error, fallback) {
    return error && error.data && error.data.errorMessage
        ? error.data.errorMessage
        : fallback;
}

app.controller('EmployeeMasterController', function ($scope, $rootScope, $state, $q, $window, EmployeeService, EmployeeSelection) {
    $scope.employees = [];
    $scope.selected = EmployeeSelection.selected;
    $scope.filters = { employeeCode: '', department: '', jobTitle: '' };
    $scope.sortField = 'employeeCode';
    $scope.sortDescending = false;
    $scope.pageNumber = 0;
    $scope.pageSize = 100;
    $scope.totalPages = 0;
    $scope.totalElements = 0;
    $scope.pageNumbers = [];
    $scope.loading = true;
    $scope.deleting = false;
    $scope.importing = false;
    $scope.selectedCount = 0;
    $scope.departments = [];
    $scope.jobTitles = [];
    $scope.error = '';
    $scope.notice = $rootScope.employeeNotice || '';
    $rootScope.employeeNotice = '';
    var loadSequence = 0;

    $scope.sortBy = function (field) {
        if ($scope.sortField === field) {
            $scope.sortDescending = !$scope.sortDescending;
        } else {
            $scope.sortField = field;
            $scope.sortDescending = false;
        }
        loadEmployees(0);
    };

    $scope.applyFilters = function () {
        loadEmployees(0);
    };

    $scope.changePage = function (page) {
        if (page >= 0 && page < $scope.totalPages && page !== $scope.pageNumber) {
            loadEmployees(page);
        }
    };

    $scope.openEmployee = function (employee) {
        $state.go('dashboard.employeeMaster.details', { employeeCode: employee.employeeCode });
    };

    $scope.toggleAll = function () {
        var selectAll = !$scope.isAllSelected();
        angular.forEach($scope.employees, function (employee) {
            $scope.selected[employee.employeeCode] = selectAll;
        });
        updateSelectionCount();
    };

    $scope.updateSelectionCount = updateSelectionCount;
    $scope.isAllSelected = function () {
        return $scope.employees.length > 0 && $scope.employees.every(function (employee) {
            return $scope.selected[employee.employeeCode];
        });
    };

    $scope.deleteSelected = function () {
        var codes = Object.keys($scope.selected).filter(function (code) {
            return $scope.selected[code];
        });
        if (!codes.length || !$scope.confirmDelete(codes.length)) {
            return;
        }

        $scope.deleting = true;
        $scope.error = '';
        $scope.notice = '';
        var deleted = [];
        var failed = [];

        function deactivateNext(index) {
            if (index >= codes.length) {
                return $q.when();
            }
            return EmployeeService.deactivate(codes[index]).then(function () {
                deleted.push(codes[index]);
                return deactivateNext(index + 1);
            }, function () {
                failed.push(codes[index]);
                return deactivateNext(index + 1);
            });
        }

        deactivateNext(0).then(function () {
            angular.forEach(deleted, function (code) {
                delete $scope.selected[code];
            });
            updateSelectionCount();
            if (!failed.length) {
                $scope.notice = deleted.length + ' employee(s) deleted.';
            }
            loadEmployees($scope.pageNumber);
            if (failed.length) {
                $scope.error = 'Could not delete employee(s): ' + failed.join(', ') + '. Please try again.';
            }
        }).finally(function () {
            $scope.deleting = false;
        });
    };

    $scope.confirmDelete = function (count) {
        return $window.confirm('Delete ' + count + ' selected employee(s)?');
    };

    $scope.importEmployees = function (file) {
        if (!file) {
            return;
        }
        $scope.importing = true;
        $scope.error = '';
        $scope.notice = '';
        EmployeeService.importFile(file).then(function (response) {
            $scope.notice = response.data.importedCount + ' employee(s) imported.';
            angular.forEach($scope.selected, function (isSelected, code) {
                delete $scope.selected[code];
            });
            updateSelectionCount();
            loadFilters();
            loadEmployees(0);
        }, function (error) {
            $scope.error = employeeErrorMessage(error, 'Unable to import employees. Check the Excel template and try again.');
        }).finally(function () {
            $scope.importing = false;
        });
    };

    function updateSelectionCount() {
        $scope.selectedCount = Object.keys($scope.selected).filter(function (code) {
            return $scope.selected[code];
        }).length;
    }

    function loadEmployees(page) {
        var sequence = ++loadSequence;
        $scope.loading = true;
        $scope.error = '';
        EmployeeService.getPage(page, $scope.filters, $scope.sortField, $scope.sortDescending).then(function (response) {
            if (sequence !== loadSequence) {
                return;
            }
            var result = response.data;
            if (!result || !angular.isArray(result.content)) {
                throw new Error('The employee list response was invalid.');
            }
            if (result.totalPages > 0 && page >= result.totalPages) {
                loadEmployees(result.totalPages - 1);
                return;
            }
            $scope.employees = result.content;
            $scope.pageNumber = result.totalPages ? result.number : 0;
            $scope.totalPages = result.totalPages;
            $scope.totalElements = result.totalElements;
            $scope.pageNumbers = [];
            var firstPage = Math.max(0, $scope.pageNumber - 2);
            var lastPage = Math.min($scope.totalPages, firstPage + 5);
            for (var index = firstPage; index < lastPage; index++) {
                $scope.pageNumbers.push(index);
            }
            angular.forEach($scope.employees, function (employee) {
                if (!$scope.selected.hasOwnProperty(employee.employeeCode)) {
                    $scope.selected[employee.employeeCode] = false;
                }
            });
            updateSelectionCount();
        }).catch(function (error) {
            if (sequence === loadSequence) {
                $scope.error = employeeErrorMessage(error, 'Unable to load employees. Please try again.');
            }
        }).finally(function () {
            if (sequence === loadSequence) {
                $scope.loading = false;
            }
        });
    }

    function loadFilters() {
        EmployeeService.getFilters().then(function (response) {
            $scope.departments = response.data.departments || [];
            $scope.jobTitles = response.data.jobTitles || [];
        }, function (error) {
            $scope.error = employeeErrorMessage(error, 'Unable to load employee filters.');
        });
    }

    loadFilters();
    loadEmployees(0);
});

app.controller('EmployeeDetailsController', function ($scope, $rootScope, $state, $stateParams, $window, EmployeeService) {
    $scope.loading = true;
    $scope.saving = false;
    $scope.deleting = false;
    $scope.error = '';
    $scope.notice = '';

    EmployeeService.get($stateParams.employeeCode).then(function (response) {
        $scope.employee = response.data;
    }, function (error) {
        $scope.error = employeeErrorMessage(error, 'Unable to load employee details. Please try again.');
    }).finally(function () {
        $scope.loading = false;
    });

    $scope.save = function () {
        if ($scope.employeeForm.$invalid) {
            return;
        }
        $scope.saving = true;
        $scope.error = '';
        $scope.notice = '';
        EmployeeService.update($scope.employee).then(function () {
            $scope.notice = 'Employee details have been saved.';
        }, function (error) {
            $scope.error = employeeErrorMessage(error, 'Unable to save employee details. Please try again.');
        }).finally(function () {
            $scope.saving = false;
        });
    };

    $scope.delete = function () {
        if (!$scope.employee || !$window.confirm('Delete employee ' + $scope.employee.employeeCode + '?')) {
            return;
        }
        $scope.deleting = true;
        $scope.error = '';
        EmployeeService.deactivate($scope.employee.employeeCode).then(function () {
            $rootScope.employeeNotice = 'Employee ' + $scope.employee.employeeCode + ' deleted.';
            return $state.go('dashboard.employeeMaster.list');
        }, function (error) {
            $scope.error = employeeErrorMessage(error, 'Unable to delete employee. Please try again.');
        }).finally(function () {
            $scope.deleting = false;
        });
    };

    $scope.cancel = function () {
        $state.go('dashboard.employeeMaster.list');
    };
});

app.controller('AddEmployeeController', function ($scope, $rootScope, $state, EmployeeService) {
    $scope.employee = {};
    $scope.saving = false;
    $scope.error = '';

    $scope.save = function () {
        if ($scope.employeeForm.$invalid) {
            return;
        }
        $scope.saving = true;
        $scope.error = '';
        EmployeeService.create($scope.employee).then(function (response) {
            $rootScope.employeeNotice = 'Employee ' + response.data.employeeCode + ' added.';
            return $state.go('dashboard.employeeMaster.list');
        }, function (error) {
            $scope.error = employeeErrorMessage(error, 'Unable to add employee. Please try again.');
        }).finally(function () {
            $scope.saving = false;
        });
    };

    $scope.cancel = function () {
        $state.go('dashboard.employeeMaster.list');
    };
});
