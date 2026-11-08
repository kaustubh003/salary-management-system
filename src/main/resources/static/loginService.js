var app = angular.module('salaryManagementApp');

app.factory('LoginService', function ($http, $rootScope) {
    var isAuthenticated = false;

    return {
        login: function (username, password) {
            return $http.post('/api/auth/login', {
                username: username,
                password: password
            }).then(function (response) {
                $rootScope.token = response.data.token;
                isAuthenticated = true;
                return true;
            });
        },
        logout: function () {
            return $http.post('/api/auth/logout').then(function () {
                isAuthenticated = false;
                $rootScope.token = null;
                $rootScope.userName = null;
                return true;
            });
        },
        isAuthenticated: function () {
            return isAuthenticated;
        }
    };
});