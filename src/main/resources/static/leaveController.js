var app = angular.module('salaryManagementApp');

var leaveTypes = ['CASUAL', 'EARNED', 'SICK', 'UNPAID'];

function leaveErrorMessage(error, fallback) {
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

function loadEmployees(LeaveService) {
    var employees = [];

    function loadPage(page) {
        return LeaveService.getEmployeePage(page).then(function (response) {
            employees = employees.concat(response.data.content || []);
            if (!response.data.last) {
                return loadPage(page + 1);
            }
            return employees;
        });
    }

    return loadPage(0);
}

function localLeaveDateString(value) {
    if (!value) {
        return null;
    }
    var year = value.getFullYear();
    var month = String(value.getMonth() + 1).padStart(2, '0');
    var day = String(value.getDate()).padStart(2, '0');
    return year + '-' + month + '-' + day;
}

app.controller('LeaveManagementController', function ($scope, $state, LeaveService, LeaveSelection) {
    $scope.leaveTypes = leaveTypes;
    $scope.employees = [];
    $scope.employeeCode = LeaveSelection.employeeCode || '';
    $scope.availableLeaves = [];
    $scope.loadingEmployees = true;
    $scope.loadingLeaves = false;
    $scope.error = '';

    loadEmployees(LeaveService).then(function (employees) {
        $scope.employees = employees;
        if ($scope.employeeCode && !$scope.employees.some(function (employee) {
            return employee.employeeCode === $scope.employeeCode;
        })) {
            $scope.employeeCode = '';
            LeaveSelection.employeeCode = '';
        }
        if ($scope.employeeCode) {
            $scope.loadAvailableLeaves();
        }
    }).catch(function (error) {
        $scope.error = leaveErrorMessage(error, 'Unable to load employees. Please try again.');
    }).finally(function () {
        $scope.loadingEmployees = false;
    });

    $scope.loadAvailableLeaves = function () {
        if (!$scope.employeeCode) {
            $scope.availableLeaves = [];
            return;
        }
        LeaveSelection.employeeCode = $scope.employeeCode;
        $scope.loadingLeaves = true;
        $scope.error = '';
        $scope.availableLeaves = [];
        LeaveService.getAvailableLeaves($scope.employeeCode).then(function (response) {
            $scope.availableLeaves = response.data;
        }).catch(function (error) {
            $scope.error = leaveErrorMessage(error, 'Unable to load available leaves. Please try again.');
        }).finally(function () {
            $scope.loadingLeaves = false;
        });
    };

    $scope.openAvailableLeaves = function (availableLeaves) {
        $state.go('dashboard.leaveManagement.details', {
            employeeCode: $scope.employeeCode,
            leaveType: availableLeaves.leaveType
        });
    };

    $scope.addLeave = function () {
        $state.go('dashboard.leaveManagement.add', { employeeCode: $scope.employeeCode });
    };

    $scope.showHistory = function () {
        $state.go('dashboard.leaveManagement.history', { employeeCode: $scope.employeeCode });
    };
});

app.controller('AvailableLeavesDetailsController', function ($scope, $state, $stateParams, LeaveService, LeaveSelection) {
    $scope.leaveTypes = leaveTypes;
    $scope.loading = true;
    $scope.saving = false;
    $scope.error = '';
    $scope.employeeCode = $stateParams.employeeCode;
    LeaveSelection.employeeCode = $scope.employeeCode;

    LeaveService.getAvailableLeave($scope.employeeCode, $stateParams.leaveType).then(function (response) {
        $scope.availableLeaves = {
            employeeCode: response.data.employeeCode || $scope.employeeCode,
            leaveType: response.data.leaveType,
            availableLeaves: response.data.availableLeaves
        };
    }).catch(function (error) {
        $scope.error = leaveErrorMessage(error, 'Unable to load available leave details. Please try again.');
    }).finally(function () {
        $scope.loading = false;
    });

    $scope.save = function (form) {
        if (!form || form.$invalid || !$scope.availableLeaves) {
            return;
        }
        $scope.saving = true;
        $scope.error = '';
        LeaveService.updateAvailableLeaves($scope.availableLeaves).catch(function () {
            return LeaveService.addAvailableLeaves($scope.availableLeaves);
        }).then(function () {
            return $state.go('dashboard.leaveManagement.list', {}, { reload: true });
        }).catch(function (error) {
            $scope.error = leaveErrorMessage(error, 'Unable to save available leaves. Please try again.');
        }).finally(function () {
            $scope.saving = false;
        });
    };

    $scope.cancel = function () {
        $state.go('dashboard.leaveManagement.list');
    };
});

app.controller('AddLeaveController', function ($scope, $state, $stateParams, LeaveService, LeaveSelection) {
    $scope.employeeCode = $stateParams.employeeCode;
    LeaveSelection.employeeCode = $scope.employeeCode;
    $scope.leaveTypes = leaveTypes;
    $scope.leave = {};
    $scope.saving = false;
    $scope.error = '';

    $scope.save = function (form) {
        if (!form || form.$invalid) {
            return;
        }
        $scope.saving = true;
        $scope.error = '';
        LeaveService.addLeave($scope.employeeCode, {
            leaveDate: localLeaveDateString($scope.leave.leaveDate),
            leaveType: $scope.leave.leaveType
        }).then(function () {
            return $state.go('dashboard.leaveManagement.history', { employeeCode: $scope.employeeCode });
        }).catch(function (error) {
            $scope.error = leaveErrorMessage(error, 'Unable to add leave. Please try again.');
        }).finally(function () {
            $scope.saving = false;
        });
    };

    $scope.cancel = function () {
        $state.go('dashboard.leaveManagement.list');
    };
});

app.controller('LeaveHistoryController', function ($scope, $state, $stateParams, LeaveService, LeaveSelection) {
    $scope.employeeCode = $stateParams.employeeCode;
    LeaveSelection.employeeCode = $scope.employeeCode;
    $scope.leaves = [];
    $scope.loading = true;
    $scope.error = '';

    LeaveService.getLeaves($scope.employeeCode).then(function (response) {
        $scope.leaves = response.data;
    }).catch(function (error) {
        $scope.error = leaveErrorMessage(error, 'Unable to load leave history. Please try again.');
    }).finally(function () {
        $scope.loading = false;
    });

    $scope.back = function () {
        $state.go('dashboard.leaveManagement.list');
    };
});
