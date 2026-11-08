var app = angular.module('salaryManagementApp');

app.factory('EmployeeService', function ($http) {
    return {
        getPage: function (page, filters, sortField, sortDescending) {
            return $http.get('/api/employees', {
                params: {
                    page: page,
                    size: 100,
                    sort: sortField + ',' + (sortDescending ? 'desc' : 'asc'),
                    employeeCode: filters.employeeCode || undefined,
                    department: filters.department || undefined,
                    jobTitle: filters.jobTitle || undefined
                }
            });
        },
        getFilters: function () {
            return $http.get('/api/employees/filters');
        },
        get: function (employeeCode) {
            return $http.get('/api/employees/' + encodeURIComponent(employeeCode));
        },
        create: function (employee) {
            return $http.post('/api/employees', employee);
        },
        update: function (employee) {
            return $http.put('/api/employees', employee);
        },
        deactivate: function (employeeCode) {
            return $http.patch('/api/employees/' + encodeURIComponent(employeeCode) + '/deactivate');
        },
        importFile: function (file) {
            var formData = new FormData();
            formData.append('file', file);
            return $http.post('/api/employees/import', formData, {
                transformRequest: angular.identity,
                headers: { 'Content-Type': undefined }
            });
        }
    };
});

app.directive('employeeFile', function () {
    return {
        scope: { onFile: '&' },
        link: function (scope, element) {
            element.on('change', function (event) {
                var file = event.target.files[0];
                if (file) {
                    scope.$apply(function () {
                        scope.onFile({ file: file });
                    });
                }
                element.val('');
            });
        }
    };
});
