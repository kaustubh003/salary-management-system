var app = angular.module('salaryManagementApp');

app.factory('PaymentService', function ($http) {
    return {
        getPayments: function (employeeCode, page, size) {
            return $http.get('/api/employees/' + encodeURIComponent(employeeCode) + '/payments', {
                params: {
                    page: page,
                    size: size,
                    sort: 'periodStart,desc'
                }
            });
        },
        getSalaries: function (employeeCode) {
            return $http.get('/api/employees/' + encodeURIComponent(employeeCode) + '/salaries');
        },
        getLeaves: function (employeeCode) {
            return $http.get('/api/employees/' + encodeURIComponent(employeeCode) + '/leaves');
        },
        getAvailableLeaves: function (employeeCode) {
            return $http.get('/api/' + encodeURIComponent(employeeCode) + '/availableLeaves');
        },
        create: function (payment) {
            return $http.post('/api/payments', payment);
        }
    };
});

app.factory('PaymentSelection', function () {
    return { employeeCode: '' };
});
