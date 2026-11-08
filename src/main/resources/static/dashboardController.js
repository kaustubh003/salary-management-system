var app = angular.module('salaryManagementApp');

app.controller('DashboardController',
    function ($scope, $rootScope, $state, LoginService) {
        $scope.user = $rootScope.userName;
        $scope.$state = $state;
        $scope.loggingOut = false;
        $scope.logoutError = '';

        $scope.logout = function () {
            $scope.loggingOut = true;
            $scope.logoutError = '';
            LoginService.logout().then(function () {
                return $state.go('login');
            }, function (error) {
                $scope.logoutError = error && error.data && error.data.errorMessage
                    ? error.data.errorMessage
                    : 'Unable to sign out. Please try again.';
            }).finally(function () {
                $scope.loggingOut = false;
            });
        };
    });

app.controller('PlaceholderController', function ($scope, $state) {
    $scope.sectionTitle = $state.current.data.pageTitle;
});