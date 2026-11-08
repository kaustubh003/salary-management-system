(function () {

    var app = angular.module("salaryManagementApp", ["ui.router"]);

    app.factory('authInterceptor', function ($rootScope) {
        return {
            request: function (config) {
                if (config.url.indexOf('/api/') === 0 &&
                    !/\/api\/auth\/login$/.test(config.url) &&
                    $rootScope.token) {
                    config.headers.Authorization = 'Bearer ' + $rootScope.token;
                }
                return config;
            }
        };
    });

    app.run(function ($rootScope, $location, $state, $transitions, LoginService) {
        console.clear();
        console.log('App started...');
        $rootScope.pageTitle = 'Salary Management App';
        $transitions.onSuccess({}, function (transition) {
            $rootScope.pageTitle = transition.to().title || 'Salary Management App';
        });
        if (!LoginService.isAuthenticated()) {
            $state.transitionTo('login');
        }
    });

    app.config(['$stateProvider', '$urlRouterProvider', '$httpProvider',
        function ($stateProvider, $urlRouterProvider, $httpProvider) {
            $httpProvider.interceptors.push('authInterceptor');
            $stateProvider
                .state('login', {
                    url: '/login',
                    templateUrl: 'views/login.html',
                    controller: 'LoginController',
                    title: 'Login - Salary Management App'
                })
                .state('dashboard', {
                    url: '/dashboard',
                    templateUrl: 'views/dashboard.html',
                    controller: 'DashboardController',
                    title: 'Dashboard - Salary Management App'
                })
                .state('dashboard.employeeMaster', {
                    url: '/employee-master',
                    abstract: true,
                    templateUrl: 'views/employee-master-layout.html',
                    data: { pageTitle: 'Employee Master' },
                    title: 'Employee Master - Salary Management App'
                })
                .state('dashboard.employeeMaster.list', {
                    url: '',
                    templateUrl: 'views/employee-list.html',
                    controller: 'EmployeeMasterController',
                    title: 'Employee Master - Salary Management App'
                })
                .state('dashboard.employeeMaster.details', {
                    url: '/:employeeCode',
                    templateUrl: 'views/employee-details.html',
                    controller: 'EmployeeDetailsController',
                    data: { pageTitle: 'Employee Details' },
                    title: 'Employee Details - Salary Management App'
                })
                .state('dashboard.employeeMaster.add', {
                    url: '/new',
                    templateUrl: 'views/employee-form.html',
                    controller: 'AddEmployeeController',
                    data: { pageTitle: 'Add Employee' },
                    title: 'Add Employee - Salary Management App'
                })
                .state('dashboard.salaryManagement', {
                    url: '/salary-management',
                    templateUrl: 'views/placeholder.html',
                    controller: 'PlaceholderController',
                    data: { pageTitle: 'Salary Management' },
                    title: 'Salary Management - Salary Management App'
                })
                .state('dashboard.leaveManagement', {
                    url: '/leave-management',
                    templateUrl: 'views/placeholder.html',
                    controller: 'PlaceholderController',
                    data: { pageTitle: 'Leave Management' },
                    title: 'Leave Management - Salary Management App'
                })
                .state('dashboard.payments', {
                    url: '/payments',
                    templateUrl: 'views/placeholder.html',
                    controller: 'PlaceholderController',
                    data: { pageTitle: 'Payments' },
                    title: 'Payments - Salary Management App'
                })
                .state('dashboard.paymentSlips', {
                    url: '/payment-slips',
                    templateUrl: 'views/placeholder.html',
                    controller: 'PlaceholderController',
                    data: { pageTitle: 'Payment Slips' },
                    title: 'Payment Slips - Salary Management App'
                });

            $urlRouterProvider.otherwise('/login');
        }]);

})();
