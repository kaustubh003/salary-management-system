var app = angular.module('salaryManagementApp');

app.factory('LoginService', function ($http, $rootScope, $window) {
    var tokenKey = 'salaryManagementToken';
    var usernameKey = 'salaryManagementUsername';
    var isAuthenticated = false;

    function clearStoredSession() {
        $window.sessionStorage.removeItem(tokenKey);
        $window.sessionStorage.removeItem(usernameKey);
    }

    function invalidateSession() {
        isAuthenticated = false;
        $rootScope.token = null;
        $rootScope.userName = null;
        clearStoredSession();
    }

    function restoreSession() {
        var token = $window.sessionStorage.getItem(tokenKey);
        if (!token) {
            clearStoredSession();
            return;
        }

        $rootScope.token = token;
        $rootScope.userName = $window.sessionStorage.getItem(usernameKey);
        isAuthenticated = true;
    }

    restoreSession();

    return {
        login: function (username, password) {
            return $http.post('/api/auth/login', {
                username: username,
                password: password
            }).then(function (response) {
                $rootScope.token = response.data.token;
                $rootScope.userName = username;
                $window.sessionStorage.setItem(tokenKey, response.data.token);
                $window.sessionStorage.setItem(usernameKey, username);
                isAuthenticated = true;
                return true;
            });
        },
        logout: function () {
            return $http.post('/api/auth/logout').then(function () {
                invalidateSession();
                return true;
            });
        },
        invalidateSession: invalidateSession,
        isAuthenticated: function () {
            return isAuthenticated;
        }
    };
});