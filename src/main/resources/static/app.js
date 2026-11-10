(function () {

    var app = angular.module("salaryManagementApp", ["ui.router"]);

    app.factory('authInterceptor', function ($rootScope, $q, $injector) {
        var redirectPromise = null;

        function isProtectedApiRequest(config) {
            var url = config && config.url ? config.url : '';
            var authorization = config && config.headers &&
                (config.headers.Authorization || config.headers.authorization);
            return /^(?:https?:\/\/[^/]+)?\/api\/(?!auth\/login(?:[?#]|$))/.test(url) &&
                typeof authorization === 'string' &&
                authorization.indexOf('Bearer ') === 0;
        }

        return {
            request: function (config) {
                if (config.url.indexOf('/api/') === 0 &&
                    !/\/api\/auth\/login$/.test(config.url) &&
                    $rootScope.token) {
                    config.headers.Authorization = 'Bearer ' + $rootScope.token;
                }
                return config;
            },
            responseError: function (response) {
                if (isProtectedApiRequest(response.config) && response.status === 401) {
                    var requestAuthorization = response.config.headers.Authorization ||
                        response.config.headers.authorization;
                    if (requestAuthorization === 'Bearer ' + $rootScope.token) {
                        var loginService = $injector.get('LoginService');
                        loginService.invalidateSession();

                        if (!redirectPromise) {
                            redirectPromise = $injector.get('$state').go('login')
                                .finally(function () {
                                    redirectPromise = null;
                                });
                        }
                    }
                }
                return $q.reject(response);
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
                    abstract: true,
                    templateUrl: 'views/salary-management-layout.html',
                    data: { pageTitle: 'Salary Management' },
                    title: 'Salary Management - Salary Management App'
                })
                .state('dashboard.salaryManagement.list', {
                    url: '',
                    templateUrl: 'views/salary-list.html',
                    controller: 'SalaryManagementController',
                    data: { pageTitle: 'Salary Management' },
                    title: 'Salary Management - Salary Management App'
                })
                .state('dashboard.salaryManagement.details', {
                    url: '/:salaryId',
                    params: { employeeCode: { value: null, squash: true } },
                    templateUrl: 'views/salary-details.html',
                    controller: 'SalaryDetailsController',
                    data: { pageTitle: 'Salary Details' },
                    title: 'Salary Details - Salary Management App'
                })
                .state('dashboard.salaryManagement.add', {
                    url: '/new',
                    params: { employeeCode: { value: null, squash: true } },
                    templateUrl: 'views/salary-form.html',
                    controller: 'AddSalaryController',
                    data: { pageTitle: 'Add Salary' },
                    title: 'Add Salary - Salary Management App'
                })
                .state('dashboard.leaveManagement', {
                    url: '/leave-management',
                    abstract: true,
                    templateUrl: 'views/leave-management-layout.html',
                    data: { pageTitle: 'Leave Management' },
                    title: 'Leave Management - Salary Management App'
                })
                .state('dashboard.leaveManagement.list', {
                    url: '',
                    templateUrl: 'views/leave-list.html',
                    controller: 'LeaveManagementController',
                    data: { pageTitle: 'Leave Management' },
                    title: 'Leave Management - Salary Management App'
                })
                .state('dashboard.leaveManagement.details', {
                    url: '/available/:employeeCode/:leaveType',
                    templateUrl: 'views/available-leaves-details.html',
                    controller: 'AvailableLeavesDetailsController',
                    data: { pageTitle: 'Available Leaves Details' },
                    title: 'Available Leaves Details - Salary Management App'
                })
                .state('dashboard.leaveManagement.add', {
                    url: '/add-leave/:employeeCode',
                    templateUrl: 'views/leave-form.html',
                    controller: 'AddLeaveController',
                    data: { pageTitle: 'Add Leave' },
                    title: 'Add Leave - Salary Management App'
                })
                .state('dashboard.leaveManagement.history', {
                    url: '/history/:employeeCode',
                    templateUrl: 'views/leave-history.html',
                    controller: 'LeaveHistoryController',
                    data: { pageTitle: 'Leave History' },
                    title: 'Leave History - Salary Management App'
                })
                .state('dashboard.payments', {
                    url: '/payments',
                    abstract: true,
                    templateUrl: 'views/payment-management-layout.html',
                    data: { pageTitle: 'Payments' },
                    title: 'Payments - Salary Management App'
                })
                .state('dashboard.payments.list', {
                    url: '',
                    templateUrl: 'views/payment-list.html',
                    controller: 'PaymentManagementController',
                    data: { pageTitle: 'Payments' },
                    title: 'Payments - Salary Management App'
                })
                .state('dashboard.payments.add', {
                    url: '/new/:employeeCode',
                    templateUrl: 'views/payment-form.html',
                    controller: 'AddPaymentController',
                    data: { pageTitle: 'Add Payment' },
                    title: 'Add Payment - Salary Management App'
                })
                .state('dashboard.paymentSlips', {
                    url: '/payment-slips',
                    abstract: true,
                    templateUrl: 'views/payslip-management-layout.html',
                    data: { pageTitle: 'Payment Slips' },
                    title: 'Payment Slips - Salary Management App'
                })
                .state('dashboard.paymentSlips.list', {
                    url: '',
                    templateUrl: 'views/payslip-list.html',
                    controller: 'PayslipManagementController',
                    data: { pageTitle: 'Payment Slips' },
                    title: 'Payment Slips - Salary Management App'
                });

            $urlRouterProvider.otherwise('/login');
        }]);

})();
