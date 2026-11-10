var app = angular.module('salaryManagementApp');

app.factory('PayslipService', function ($http) {
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
        generate: function (paymentId) {
            return $http.post('/api/payments/' + encodeURIComponent(paymentId) + '/payslip');
        },
        generateAll: function () {
            return $http.post('/api/payslips/generate-all');
        },
        download: function (paymentId) {
            return $http.get('/api/payments/' + encodeURIComponent(paymentId) + '/payslip', {
                responseType: 'blob'
            });
        }
    };
});

app.factory('PayslipSelection', function () {
    return { employeeCode: '', selected: {} };
});
