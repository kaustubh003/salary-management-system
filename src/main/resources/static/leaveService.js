var app = angular.module('salaryManagementApp');

app.factory('LeaveService', function ($http) {
    return {
        getEmployeePage: function (page) {
            return $http.get('/api/employees', {
                params: {
                    page: page,
                    size: 100,
                    sort: 'employeeCode,asc'
                }
            });
        },
        getAvailableLeaves: function (employeeCode) {
            return $http.get('/api/' + encodeURIComponent(employeeCode) + '/availableLeaves');
        },
        getAvailableLeave: function (employeeCode, leaveType) {
            return $http.get('/api/' + encodeURIComponent(employeeCode) + '/availableLeaves', {
                params: { leaveType: leaveType }
            });
        },
        updateAvailableLeaves: function (availableLeaves) {
            return $http.put('/api/availableLeaves/updateAvailableLeaves', availableLeaves);
        },
        addAvailableLeaves: function (availableLeaves) {
            return $http.post('/api/availableLeaves/addAvailableLeaves', availableLeaves);
        },
        getLeaves: function (employeeCode) {
            return $http.get('/api/employees/' + encodeURIComponent(employeeCode) + '/leaves');
        },
        addLeave: function (employeeCode, leave) {
            return $http.post('/api/employees/' + encodeURIComponent(employeeCode) + '/leaves', leave);
        }
    };
});

app.factory('LeaveSelection', function () {
    return { employeeCode: '' };
});
