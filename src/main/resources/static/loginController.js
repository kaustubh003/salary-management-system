var app = angular.module('salaryManagementApp');
app.controller('LoginController', function ($scope, $rootScope, $stateParams, $state, LoginService) {
    $rootScope.title = "Login - Salary Management App";

    $scope.formSubmit = function () {
        $scope.error = '';
        $scope.isSubmitting = true;

        return LoginService.login($scope.username, $scope.password)
            .then(function () {
                $rootScope.userName = $scope.username;
                $scope.username = '';
                $scope.password = '';
                return $state.transitionTo('dashboard');
            }, function (error) {
                $scope.error = error.status === 401
                    ? "Incorrect username or password!"
                    : "Unable to log in. Please try again.";
            })
            .finally(function () {
                $scope.isSubmitting = false;
            });
    };
});