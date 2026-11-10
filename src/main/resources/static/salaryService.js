var app = angular.module('salaryManagementApp');

app.factory('SalaryService', function ($http) {
    return {
        getEmployee: function (employeeCode) {
            return $http.get('/api/employees/' + encodeURIComponent(employeeCode));
        },
        getForEmployee: function (employeeCode) {
            return $http.get('/api/employees/' + encodeURIComponent(employeeCode) + '/salaries');
        },
        get: function (salaryId) {
            return $http.get('/api/salaries/' + encodeURIComponent(salaryId));
        },
        create: function (employeeCode, salary) {
            return $http.post('/api/employees/' + encodeURIComponent(employeeCode) + '/salaries', salary);
        },
        update: function (salaryId, salary) {
            return $http.put('/api/salaries/' + encodeURIComponent(salaryId), salary);
        },
        delete: function (salaryId) {
            return $http.delete('/api/salaries/' + encodeURIComponent(salaryId));
        }
    };
});

app.factory('SalarySelection', function () {
    return { selected: {}, employeeCode: '' };
});
