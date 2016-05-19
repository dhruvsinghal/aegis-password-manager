var aegisApp = angular.module('aegisApp', [
  'crypto',
  'ngRoute',
  'aegisControllers'
]);

aegisApp.config(['$routeProvider', '$httpProvider',
  function ($routeProvider, $httpProvider) {
    $httpProvider.defaults.withCredentials = true;
    $routeProvider.when('/signup1', {
      templateUrl: 'partials/signup1.html',
      controller: 'Signup1Ctrl'
    }).when('/signup2', {
      templateUrl: 'partials/signup2.html',
      controller: 'Signup2Ctrl'
    }).when('/changepassword', {
      templateUrl: 'partials/changepassword.html',
      controller: 'ChangeMPCtrl'
    }).when('/publickey', {
      templateUrl: 'partials/publickey.html',
      controller: 'PublicKeyCtrl'
    }).when('/deleteuser', {
      templateUrl: 'partials/deleteuser.html',
      controller: 'DeleteUserCtrl'
    }).when('/login', {
      templateUrl: 'partials/login.html',
      controller: 'LoginCtrl'
    }).when('/vault/:teamid', {
      templateUrl: 'partials/vault.html',
      controller: 'VaultCtrl'
    }).when('/logs/user', {
      templateUrl: 'partials/logs.html',
      controller: 'UserLogCtrl'
    }).when('/logs/teams/:teamid', {
      templateUrl: 'partials/logs.html',
      controller: 'TeamLogCtrl'
    }).when('/teams', {
      templateUrl: 'partials/teams.html',
      controller: 'TeamCtrl'
    }).otherwise({
      redirectTo: '/signup1'
    });
  }]);

var aegisControllers = angular.module('aegisControllers', ['crypto']);

/**
 * Shows an HTML5 alert box to the user. Note that the object passed in to the function must
 * be an Angular $http response object.
 */
var errorHandler = function(response) {
  alert('ERROR: ' + response.data);
};

/**
 * This function fetches the list of teams the current user is a part of and puts
 * it in the scope variable teams.
 */
var fillInTeams = function ($scope, $http) {
  console.log('Filling in teams...');
  // Fill in teams
  $http.get('/teams').then(function (response) {
    if (response.status === 200) {
      console.log('Fetched teams.');
      $scope.teams = response.data;
    } else {
      errorHandler(response);
    }
  }, errorHandler);
};

/**
 * This function checks if the session is valid. If it is, it loads the given function,
 * otherwise it redirects the user to the login page.
 */
var checkLoggedInAndLoad = function ($http, $location, $scope, functionToLoad) {
  $scope.loggedIn = false;

  $scope.logout = function () {
    $http.get('/logout').then(function () {
      $location.path('/login');
    });
  };

  var routeToLogin = function () {
    $location.path('/login');
  };

  $http.get('/sessionValid').then(function (response) {
    if (response.data) {
      console.log('Session is still valid.');
      $scope.loggedIn = true;
      functionToLoad();
    } else {
      console.log('Server says the session is not valid.');
      routeToLogin();
    }
  }, routeToLogin);
};

/**
 * This function classifies a password as strong or weak.
 */
var isPasswordStrong = function (password) {
  if (password.length >= 16) {
    return true;
  } else if (password.match(/\d/) != null && password.match(/[A-Z]/) != null &&
      password.match(/[a-z]/) != null && password.match(/\W/) != null) {
    return true;
  } else {
    return false;
  }
};

aegisControllers.controller('Signup1Ctrl', ['$scope', 'crypto', function ($scope, crypto) {
    $scope.email = '';
    $scope.doSignup1 = function () {
      console.log("Entered email was " + $scope.email);
      if ($scope.email) {
        crypto.initiateSignup($scope.email, '/signup2', errorHandler);
      }
    };

  }]);

aegisControllers.controller('ChangeMPCtrl', ['$scope', '$routeParams', '$http', '$location',
  'crypto', function ($scope, $routeParams, $http, $location, crypto) {
    checkLoggedInAndLoad($http, $location, $scope, function () {
      $scope.password = '';
      $scope.isPasswordStrong = false;

      fillInTeams($scope, $http);

      $scope.$watch('password', function () {
        if ($scope.password) {
          if (isPasswordStrong($scope.password)) {
            $scope.passwordClass = 'strong-password';
          } else {
            $scope.passwordClass = 'weak-password';
          }
        } else {
          $scope.passwordClass = null;
        }
      });

      $scope.changeMP = function () {
        crypto.changeMasterPassword($scope.password, '/logout', errorHandler);
      };
    });
  }]);

aegisControllers.controller('PublicKeyCtrl', ['$scope', '$http', '$location', 'crypto',
  function ($scope, $http, $location, crypto) {
    checkLoggedInAndLoad($http, $location, $scope, function () {
      fillInTeams($scope, $http);
      crypto.verifyAndShowPublicKey($scope, errorHandler);
    });
  }]);

aegisControllers.controller('DeleteUserCtrl', ['$scope', '$rootScope', '$http', '$location',
  'crypto', function ($scope, $rootScope, $http, $location, crypto) {
    checkLoggedInAndLoad($http, $location, $scope, function () {
      fillInTeams($scope, $http);
      $scope.deleteUser = function() {
        if (!$rootScope.masterPassword || $rootScope.masterPassword === $scope.confirmMasterPassword) {
          $http.delete('/users').then(function (response) {
            if (response.status === 200) {
              $location.path('/login');
            } else {
              console.log('Delete user failed.');
              errorHandler(response);
            }
          }, errorHandler);
        } else {
          alert('Invalid master password.');
        }
      };
    }, errorHandler);
  }]);

aegisControllers.controller('Signup2Ctrl', ['$scope', '$routeParams', '$http', '$location',
  'crypto', function ($scope, $routeParams, $http, $location, crypto) {
    $scope.firstname = '';
    $scope.lastname = '';
    $scope.password = '';
    $scope.code = '';

    $scope.$watch('password', function () {
      if ($scope.password) {
        if (isPasswordStrong($scope.password)) {
          $scope.passwordClass = 'strong-password';
        } else {
          $scope.passwordClass = 'weak-password';
        }
      } else {
        $scope.passwordClass = null;
      }
    });

    $scope.doSignup2 = function () {
      crypto.completeSignup($scope.firstname, $scope.lastname, $scope.password, $scope.code,
        '/login', errorHandler);
    };
  }]);

aegisControllers.controller('LoginCtrl', ['$scope', 'crypto',
  function ($scope, crypto) {
    $scope.name = '';
    $scope.password = '';

    $scope.doLogin = function () {
      crypto.login($scope.email, $scope.password, '/teams', errorHandler);
    }
  }]);

aegisControllers.controller('VaultCtrl', ['$scope', '$routeParams', '$http', '$location', 'crypto',
  function ($scope, $routeParams, $http, $location, crypto) {

    checkLoggedInAndLoad($http, $location, $scope, function () {
      fillInTeams($scope, $http);

      $scope.newTitle = '';
      $scope.newUsername = '';
      $scope.newPassword = '';

      var refreshEntries = function () {
        $scope.newTitle = '';
        $scope.newUsername = '';
        $scope.newPassword = '';
        crypto.readEntries($scope, $routeParams['teamid'], null);
        $http.get('/teams/' + $routeParams['teamid']).then(function(response) {
          $scope.currentTeam = response.data;
        }, errorHandler);
      };

      refreshEntries();

      $scope.addEntry = function () {
        crypto.addEntry($scope.newTitle, $scope.newUsername,
          $scope.newPassword, $routeParams['teamid'], refreshEntries, errorHandler);
      };

      $scope.saveEntry = function (id, title, username, password) {
        $http.delete('/teams/' + $routeParams['teamid'] + '/entries/' + id).then(function () {
          crypto.addEntry(title, username, password, $routeParams['teamid'], refreshEntries,
              errorHandler);
        }, errorHandler);
      };

      $scope.deleteEntry = function (id) {
        $http.delete('/teams/' + $routeParams['teamid'] + '/entries/' + id).then(
          refreshEntries, errorHandler);
      };
    });
  }]);

aegisControllers.controller('EntryCtrl', ['$scope', '$routeParams', '$http', '$location',
  function ($scope, $routeParams, $http, $location) {
    checkLoggedInAndLoad($http, $location, $scope, function () {
      fillInTeams($scope, $http);
      var entryId = $routeParams.entryid;
      $scope.password_field_type = 'password';
      $scope.$watch('show_password', function (value) {
        if (!value) {
          $scope.password_field_type = 'password';
        } else {
          $scope.password_field_type = 'text';
        }
      });
    });
  }]);

aegisControllers.controller('TeamCtrl', ['$scope', '$route', '$routeParams', '$http', '$location',
  'crypto', function ($scope, $route, $routeParams, $http, $location, crypto) {

    checkLoggedInAndLoad($http, $location, $scope, function () {
      // Initialize the scope models
      $scope.creation = {
        teamName: ''
      };
      $scope.teams = [];
      $scope.selectedTeam = 0;
      $scope.newUserEmail = '';
      $scope.newUserPermission = 'READ';

      fillInTeams($scope, $http);

      var refreshDisplayedTeams = function () {
        console.log('Selected teams changed to ' + $scope.selectedTeam + ' ...');
        if ($scope.selectedTeam) {
          $http.get('/teams/' + $scope.selectedTeam + '/users').then(function (response) {
            if (response.status === 403) {
              $scope.users = null;
            } else {
              $scope.users = response.data;
            }
          })
        }
      };

      $scope.createTeam = function () {
        name = $scope.creation.teamName;
        console.log('Trying to create a team named ' + name + ' ...');
        crypto.createTeam($scope.creation.teamName, function () {
          fillInTeams($scope, $http);
        }, errorHandler);
      };

      $scope.addUser = function () {
        crypto.addUserToTeam($scope.selectedTeam, $scope.newUserEmail,
          $scope.newUserPermission, refreshDisplayedTeams, errorHandler);
      };

      $scope.deleteUser = function (id) {
        $http.delete('/teams/' + $scope.selectedTeam + '/users/' + id).then(function () {
          alert("Please note that this user could potentially still gain access to passwords in this" +
              "team if they have access to the database. Please remake the team if you are worried " +
              "about this threat");
          refreshDisplayedTeams();
        }, errorHandler);
      };

      $scope.deleteTeam = function (id) {
        $http.delete('/teams/' + $scope.selectedTeam).then(function() {
          $route.reload();
        }, errorHandler);
      };

      $scope.permissionChanged = function (email, perm) {
        crypto.changeTeamPermission(email, $scope.selectedTeam, perm, refreshDisplayedTeams,
          errorHandler);
      };

      $scope.$watch('selectedTeam', refreshDisplayedTeams);
    });
  }]);

aegisControllers.controller('UserLogCtrl', ['$scope', '$http', '$location',
  '$routeParams', function ($scope, $http, $location, $routeParams) {

    checkLoggedInAndLoad($http, $location, $scope, function () {
      fillInTeams($scope, $http);
      $http.get('/logs/user').then(
        function (response) {
          $scope.logs = response.data;
        });
    });
  }]);

aegisControllers.controller('TeamLogCtrl', ['$scope', '$http', '$location',
  '$routeParams', function ($scope, $http, $location, $routeParams) {
    checkLoggedInAndLoad($http, $location, $scope, function () {
      fillInTeams($scope, $http);
      $http.get('/logs/teams/' + $routeParams['teamid']).then(
        function (response) {
          $scope.logs = response.data;
        });
    });
  }]);
