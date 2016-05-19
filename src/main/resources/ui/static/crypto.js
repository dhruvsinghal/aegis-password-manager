/**
 * Created by Dhruv Singhal on 2016-04-23.
 */

/* Cryptographic meta-constants. */
var PBKDF2_KEY_ITERATIONS = 20000;
var PBKDF2_KEY_PASSWORD_HASH = 22000;
var SALT_LENGTH = 32; // 32 bytes = 256 bits
var KEY_LENGTH = 32; // 32 bytes = 256 bits
var IV_LENGTH = 16; // 16 bytes = 128 bits
var RSA_LENGTH = 2048; // bits

var FAIL_USER_RETRIEVAL_MESSAGE = 'Error retrieving the current user.';
var FAIL_TEAM_RETRIEVAL_MESSAGE = 'Error retrieving the specified team.';

/**
 * Constructs an object that encapsulates both the raw bytes and the hexadecimal encoding of the
 * data represented.
 * @param bytes The raw bytes.
 * @param hex Hexadecimal in lowercase alphabet.
 * @constructor
 */
function DataBlock(bytes, hex) {
  this.bytes = bytes;
  this.hex = hex;
}

var aegisUtils = {
  /* Generates a block of random bytes of the given length (in bytes). */
  generateRandomBlock: function (len) {
    bytes = forge.random.getBytesSync(len);
    return new DataBlock(bytes, forge.util.bytesToHex(bytes));
  },

  /* Generates a salt of the default length (specified in the constants block). */
  genSalt: function () {
    return aegisUtils.generateRandomBlock(SALT_LENGTH);
  },

  /* Generates an IV of the default length (specified in the constants block). */
  genIv: function () {
    return aegisUtils.generateRandomBlock(IV_LENGTH);
  },

  /* Generates an AES key of the default length (specified in the constants block). */
  genAESKey: function () {
    return aegisUtils.generateRandomBlock(KEY_LENGTH);
  },

  /* Generates an RSA key-pair using the default key size (speficied in the constants block). */
  genRSA: function () {
    var pair = forge.pki.rsa.generateKeyPair({
      bits: RSA_LENGTH
    });
    return {
      publicKey: pair.publicKey,
      publicKeyPEM: forge.pki.publicKeyToPem(pair.publicKey),
      privateKey: pair.privateKey,
      privateKeyPEM: forge.pki.privateKeyToPem(pair.privateKey)
    };
  },

  /* Derives a key (and returns it as a DataBlock object) of the default length (specified in the
   * constants block) using PBKDF2 using the default number of iterations (also specified in the
   * constants block) and the given salt, which must be a DataBlock. */
  deriveKey: function (password, salt) {
    var bytes = forge.pkcs5.pbkdf2(password, salt.bytes, PBKDF2_KEY_ITERATIONS, KEY_LENGTH);
    return new DataBlock(bytes, forge.util.bytesToHex(bytes));
  },
  
  /* Encrypt the given plaintext using the given key and IV. Note: key and IV must be DataBlocks.
   * The returned string is in hexadecimal. */
  encryptAES: function (key, iv, plain) {
    var cipher = forge.cipher.createCipher('AES-CBC', key.bytes);
    cipher.start({
      iv: iv.bytes
    });
    cipher.update(forge.util.createBuffer(plain));
    cipher.finish();
    return cipher.output.toHex();
  },

  /* Decrypt the given ciphertext using the given key and IV. Note: key and IV must be
   * DataBlocks and the ciphertext is assumed to be a hex string. */
  decryptAES: function (key, iv, ciphertext) {
    var bytes = forge.util.hexToBytes(ciphertext);
    var cipher = forge.cipher.createDecipher('AES-CBC', key.bytes);
    cipher.start({
      iv: iv.bytes
    });
    cipher.update(forge.util.createBuffer(bytes));
    cipher.finish();
    return cipher.output.data;
  },
  
  /* Hashes the given string after concatenating the salt bytes to the string. Salt must be a
   * DataBlock object. Uses PBKDF2 and returns the result as a hexadecimal string. */
  hash: function (text, salt) {
    var bytes = forge.pkcs5.pbkdf2(text, salt.bytes, PBKDF2_KEY_PASSWORD_HASH, KEY_LENGTH);
    return forge.util.bytesToHex(bytes);
  },

  /* Hashes the given string usding SHA256, truncates the digest to 128-bits. and returns the
   result in base 64 encoding to make it easier for visual comparison. */
  shortHash: function (text) {
    var md = forge.md.sha256.create();
    md.update(text);
    return forge.util.encode64(md.digest().bytes().substring(0, 16));
  },

  /* Computes the SHA-256 HMAC of the with the given key of the given text. Note: The key must
   * be a DataBlock object and the returned result is in hexadecimal. */
  hmac: function (key, text) {
    var hmac = forge.hmac.create();
    hmac.start('sha256', key.bytes);
    hmac.update(text);
    return hmac.digest().toHex();
  },
  
  /* Encodes a raw buffer into a DataBlock object. */
  encode: function(bytes) {
    return new DataBlock(bytes, forge.util.bytesToHex(bytes));
  },

  /* Decodes a hexadecimal encoded data into a DataBlock object. */
  decode: function (hex) {
    bytes = forge.util.hexToBytes(hex);
    return new DataBlock(bytes, hex);
  },

  /* Decodes a PEM encoded RSA public key into a custom object. */
  decodePublicKey: function (keyPem) {
    var raw = forge.pki.publicKeyFromPem(keyPem);
    return {
      publicKey: raw,
      publicKeyPEM: keyPem
    };
  },

  /* Decodes a PEM encoded RSA private key into a custom object. */
  decodePrivateKey: function (keyPem) {
    var raw = forge.pki.privateKeyFromPem(keyPem);
    return {
      privateKey: raw,
      privateKeyPEM: keyPem
    };
  },

  getMasterPassword: function($rootScope) {
    if (!$rootScope.masterPassword) {
      var pwd = prompt("Please enter your master password (WARNING: plaintext):", "");
      $rootScope.masterPassword = pwd;
    }
    return $rootScope.masterPassword;
  },

  getMasterPasswordModal: function($rootScope, callback) {

  },
  
  getPrivateKeyFromUser: function (fullUser, masterPassword) {
    var salt = aegisUtils.decode(fullUser.encKeyDerivationSalt);
    console.log('Decoded Encryption key derivation salt.');

    var key = aegisUtils.deriveKey(masterPassword, salt);
    console.log('Derived encryption key.');

    var encIv = aegisUtils.decode(fullUser.encIv);
    console.log('Decoded encryption IV.');

    var privKeyPEM = aegisUtils.decryptAES(key, encIv, fullUser.privateKey);
    console.log('Decrypted private key.');

    var privateKey = aegisUtils.decodePrivateKey(privKeyPEM);
    console.log('Decoded private key PEM.');

    return privateKey.privateKey;
  },

  verifyReceivedPublicKey: function ($rootScope, me) {
    // check integrity of public key of the user (received from S)
    var oldMacKey = aegisUtils.deriveKey(aegisUtils.getMasterPassword($rootScope),
      aegisUtils.decode(me.macKeyDerivationSalt));
    var oldPublicKeyHMAC = aegisUtils.hmac(oldMacKey, me.publicKey); // already in hex
    if (me.publicKeyMAC !== oldPublicKeyHMAC) {
      alert('Fatal Error: Your public key received from the server seems to have been tampered' +
        ' with by a middle-man. Someone ' + 'may be trying to break-in to your account. Please' +
      ' report this incident to Aegis immediately.');
      throw 'Middle-man has modified the user\'s public key!';
    }
  }
};

/* Note: The factory() will inject $http and $location into this method at runtime.
 * http://weblogs.asp.net/dwahlin/using-an-angularjs-factory-to-interact-with-a-restful-service */

angular.module('crypto', [])
  .factory('crypto', ['$http', '$location', '$rootScope',
    function ($http, $location, $rootScope) {
      return {
        /* First step of signing up. Used to verify email address via the backend.
         *
         * Arguments:
         *     emailAddress - the email address to register.
         *     routeToPageOnSuccess - The page to redirect when the email address was accepted
         *         by the backend.
         *     execOnError - A function to execute in case of an error. The function can take
         *         the error message as a parameter.
         */
        initiateSignup: function (emailAddress, routeToPageOnSuccess, execOnError) {
          console.log("Inside inititateSignup");
          $http.post('/verification', JSON.stringify({
            email: emailAddress
          })).then(
            function (response) {
              if (response.status === 200) {
                console.log('InitiateSignup successful.');
                $location.path(routeToPageOnSuccess);
              } else {
                console.log('InitiateSignup request failed.');
                if (execOnError) {
                  execOnError(response.data);
                }
              }
            }, execOnError);
        },

        /* Second step of signing up. After the user has received the email verification code
         * let the user enter her information and create an account.
         *
         * Arguments:
         *     firstName - User's first name.
         *     lastName - User's last name.
         *     password - User's password.
         *     verificationCode - The verification code received by the user via email.
         *     routeToPageOnSuccess - The page to redirect when the email address was accepted
         *         by the backend.
         *     execOnError - A function to execute in case of an error. The function can take
         *         the error message as a parameter.
         */
        completeSignup: function (firstName, lastName, password, verificationCode,
                                  routeToPageOnSuccess, execOnError) {
          var masterPasswordSalt = aegisUtils.genSalt();
          console.log("Generated master password salt.");

          var encKeyDerivationSalt = aegisUtils.genSalt();
          console.log("Generated encryption key derivation salt.");

          var macKeyDerivationSalt = aegisUtils.genSalt();
          console.log("Generated public key MAC derivation salt.");

          var hashedMasterPassword = aegisUtils.hash(password, masterPasswordSalt);
          console.log("Hashed master password.");

          var encKey = aegisUtils.deriveKey(password, encKeyDerivationSalt);
          console.log("Derived encryption key.");

          var encIv = aegisUtils.genIv();
          console.log("Generated IV for public key.");

          var macKey = aegisUtils.deriveKey(password, macKeyDerivationSalt);
          console.log("Derived MAC key.");

          var rsa = aegisUtils.genRSA();
          console.log("Generated RSA key pair.");

          // already in hex
          var encryptedPrivateKey = aegisUtils.encryptAES(encKey, encIv, rsa.privateKeyPEM);
          console.log("Encrypted the private key.");

          var publicKeyHMAC = aegisUtils.hmac(macKey, rsa.publicKeyPEM); // already in hex
          console.log("HMACed the public key.");

          var message = {
            firstName: firstName,
            lastName: lastName,
            hashedMasterPassword: hashedMasterPassword,
            masterPasswordSalt: masterPasswordSalt.hex,
            encKeyDerivationSalt: encKeyDerivationSalt.hex,
            macKeyDerivationSalt: macKeyDerivationSalt.hex,
            encIv: encIv.hex,
            publicKey: rsa.publicKeyPEM,
            publicKeyMAC: publicKeyHMAC, // already in hex
            privateKey: encryptedPrivateKey, // already in hex
            code: verificationCode
          };
          $http.post('/users', JSON.stringify(message)).then(function (response) {
            if (response.status == 200) {
              $location.path(routeToPageOnSuccess);
            } else if (execOnError) {
              execOnError(response);
            }
          }, execOnError);
        },
        
        login: function (email, password, routeToPageOnSuccess, execOnError) {
          $http.post('/login1', {
            email: email
          }).then(function (response) {
            if (response.data.salt) {
              var salt = aegisUtils.decode(response.data.salt);
              var hashedPassword = aegisUtils.hash(password, salt);
              var verifCode = prompt('Please enter the verification code:');
              $http.post('/login2', {
                email: email,
                code: verifCode,
                hashedMasterPassword: hashedPassword
              }).then(
                function (response) {
                  if (response.status === 200) {
                    $rootScope.masterPassword = password;
                    $location.path(routeToPageOnSuccess);
                  } else {
                    console.log('Login 2 request failed.');
                    if (execOnError) {
                      execOnError(response);
                    }
                  }
                }, execOnError);
            } else if (execOnError) {
              execOnError();
            }
          }, execOnError);
        },

        createTeam: function (teamName, execOnSuccess, execOnError) {
          $http.get('/user').then(function (response) {
            if (response.status === 200) {
              var me = response.data;

              var teamKey = aegisUtils.genAESKey();
              console.log('Generated team symmetric key.');

              var pubKey = aegisUtils.decodePublicKey(me.publicKey);
              console.log('Decoded the public key.');

              var hmacKey = aegisUtils.deriveKey(aegisUtils.getMasterPassword($rootScope),
                aegisUtils.decode(me.macKeyDerivationSalt));
              console.log('Derived MAC key from master password.');

              var hmac = aegisUtils.hmac(hmacKey, me.publicKey);
              console.log('Computed HMAC of the public key.');

              aegisUtils.verifyReceivedPublicKey($rootScope, me);

              var encryptedTeamKey = forge.util.bytesToHex(pubKey.publicKey.encrypt(
                teamKey.bytes));
              console.log('Encrypted the team key with the user\'s public key.');

              $http.post('/teams', {
                teamName: teamName,
                teamKey: encryptedTeamKey
              }).then(
                function (response) {
                  if (response.status === 200) {
                    console.log('Team was created successfully.');
                    execOnSuccess();
                  } else {
                    console.log('Create team post request failed.');
                    if (execOnError) {
                      execOnError(response);
                    }
                  }
                }, execOnError);
            } else {
              console.log(FAIL_USER_RETRIEVAL_MESSAGE);
              if (execOnError) {
                execOnError(response);
              }
            }
          });
        },

        addUserToTeam: function (tid, userEmail, permission, execOnSuccess, execOnError) {
          $http.get('/user').then(function (response) {
            if (response.status === 200) {
              console.log('Retrieved the current user.');
              var me = response.data;
              var privateKey = aegisUtils.getPrivateKeyFromUser(me,
                aegisUtils.getMasterPassword($rootScope));
              $http.get('/teams/' + tid).then(function (response) {
                if (response.status === 200) {
                  // C asks S for encypted TKey and rest of team information (C already has it).
                  console.log('Retrieved the selected team.');

                  var team = response.data;
                  var unencryptedTeamKey = privateKey.decrypt(forge.util.hexToBytes(team.teamKey));

                  // C asks S to get UPubKey of user being added to the team (Get Partial User).
                  $http.get('/user/email/' + userEmail).then(function (response) {
                    if (response.status === 200) {
                      console.log('Retrieved the user to be added.');

                      var user = response.data;
                      var publicKey = aegisUtils.decodePublicKey(user.publicKey);

                      // C encrypts TKey with UPubKey of user to be added.
                      var encryptedTeamKey = forge.util.bytesToHex(
                        publicKey.publicKey.encrypt(unencryptedTeamKey));

                      var dispkey = aegisUtils.shortHash(user.publicKey);

                      // C sends encrypted TKey to S (refer to AddUserToTeam JSON).
                      if (confirm('Please verify with ' + user.firstName + ' ' + user.lastName +
                          ' that the given public key digest is correct: \n\n' + dispkey +
                          '\n\n To verify, ask ' + user.firstName + ' to click on the ' +
                          '"My Public Key" tab on the side bar and check if this matches the ' +
                          'key shown above. If the keys don\'t match, ' + user.firstName + '\'s' +
                          ' account may have been compromised and you should notify Aegis of it ' +
                          'immediately.')) {
                        $http.post('/teams/modify/adduser', {
                          uid: user.id,
                          tid: tid,
                          permissions: permission,
                          teamKey: encryptedTeamKey
                        }).then(function (response) {
                          if (response.status === 200) {
                            execOnSuccess(response);
                          } else {
                            console.log('Could not add user ' +
                              user.firstName + ' to teamname ' +
                              team.name);
                            execOnError(response);
                          }
                        }, execOnError);
                      } else {
                        console.log('The user could not verify the key.');
                        if (execOnError) {
                          execOnError();
                        }
                      }
                    } else {
                      console.log(FAIL_USER_RETRIEVAL_MESSAGE);
                    }
                  }, execOnError);

                }
              }, execOnError);
            }
          }, execOnError);
        },

        changeTeamPermission: function (email, tid, perm, execOnSuccess, execOnError) {
          $http.get('/user/email/' + email).then(function (response) {
            if (response.status == 200) {
              console.log('Retrieved the user to change permission of.');
              var user = response.data;
              $http.post('/teams/modify/permissions', {
                uid: user.id,
                tid: tid,
                permissions: perm
              }).then(function (response) {
                console.log('Successfully changed permission.');
                execOnSuccess();
              }, execOnError);
            } else {
              console.log('Failed to fetch the selected user from server.');
              if (execOnError) {
                execOnError(response);
              }
            }
          }, execOnError);
        },

        addEntry: function (title, username, password, tid, execOnSuccess, execOnError) {
          $http.get('/user').then(function (response) {
            if (response.status === 200) {
              var me = response.data;
              var privateKey = aegisUtils.getPrivateKeyFromUser(me,
                aegisUtils.getMasterPassword($rootScope));
              $http.get('/teams/' + tid).then(function (response) {
                if (response.status === 200) {
                  var team = response.data;
                  // C decrypts TKey by using decrypted UPrivKey.
                  var encryptedTeamKey = forge.util.hexToBytes(team.teamKey);
                  var unencryptedTeamKey = aegisUtils.encode(privateKey.decrypt(encryptedTeamKey));
                  // C encrypts the entry password with TKey.
                  var iv = aegisUtils.genIv();
                  // C sends title, username, encrypted entry password and IV to S.
                  var message = {
                    title: title,
                    username: username,
                    password: aegisUtils.encryptAES(unencryptedTeamKey, iv, password),
                    iv: iv.hex,
                  };
                  $http.post('/teams/' + tid + '/entries', JSON.stringify(message)).then(
                    function (response) {
                    if (response.status == 200) {
                      console.log('Successfully created an entry.');
                      execOnSuccess(response);
                    } else {
                      console.log('Could not post create entry request to the server.');
                      if (execOnError) {
                        execOnError(response);
                      }
                    }
                  }, execOnError);
                } else {
                  console.log(FAIL_TEAM_RETRIEVAL_MESSAGE);
                  if (execOnError) {
                    execOnError(response);
                  }
                }
              });
            } else {
              console.log(FAIL_USER_RETRIEVAL_MESSAGE);
              if (execOnError) {
                execOnError(response);
              }
            }
          }, execOnError);
        },

        readEntries: function ($scope, tid, execOnError) {
          $http.get('/user').then(function (response) {
            if (response.status === 200) {
              var me = response.data;
              var privateKey = aegisUtils.getPrivateKeyFromUser(me,
                aegisUtils.getMasterPassword($rootScope));
              $http.get('/teams/' + tid).then(function (response) {
                if (response.status === 200) {
                  var team = response.data;
                  var unencryptedTeamKey = aegisUtils.encode(privateKey.decrypt(
                    forge.util.hexToBytes(team.teamKey)));
                  $http.get('/teams/' + tid + '/entries').then(
                    function (response) {
                      if (response.status == 200) {
                        $scope.entries = response.data;
                        for (var i = $scope.entries.length - 1; i >= 0; i--) {
                          $scope.entries[i].password = aegisUtils.decryptAES(unencryptedTeamKey,
                            aegisUtils.decode(response.data[i].iv), response.data[i].password);
                        }
                        console.log($scope.entries);
                      } else if (execOnError) {
                        console.log('Could not get entries for the team.');
                        execOnError(response);
                      }
                    });
                } else {
                  console.log(FAIL_TEAM_RETRIEVAL_MESSAGE);
                  if (execOnError) {
                    execOnError(response);
                  }
                }
              });
            } else {
              console.log(FAIL_USER_RETRIEVAL_MESSAGE);
              if (execOnError) {
                execOnError(response);
              }
            }
          }, execOnError);
        },

        verifyAndShowPublicKey: function($scope, execOnError) {
          $http.get('/user').then(function(response) {
            if (response.status === 200) {
              aegisUtils.verifyReceivedPublicKey($rootScope, response.data);
              $scope.publicKey = aegisUtils.shortHash(response.data.publicKey);
            } else if (execOnError) {
              execOnError(response);
            }
          }, execOnError);
        },

        changeMasterPassword: function (password, routeToPageOnSuccess, execOnError) {
          $http.get('/user').then(function (response) {
            if (response.status === 200) {
              var me = response.data;

              aegisUtils.verifyReceivedPublicKey($rootScope, me);

              var privateKey = aegisUtils.getPrivateKeyFromUser(me,
                aegisUtils.getMasterPassword($rootScope));

              var masterPasswordSalt = aegisUtils.genSalt();
              console.log("Generated master password salt.");

              var encKeyDerivationSalt = aegisUtils.genSalt();
              console.log("Generated encryption key derivation salt.");

              var macKeyDerivationSalt = aegisUtils.genSalt();
              console.log("Generated public key MAC derivation salt.");

              var hashedMasterPassword = aegisUtils.hash(password, masterPasswordSalt);
              console.log("Hashed master password.");

              var encKey = aegisUtils.deriveKey(password, encKeyDerivationSalt);
              console.log("Derived encryption key.");

              var encIv = aegisUtils.genIv();
              console.log("Generated IV for public key.");

              var macKey = aegisUtils.deriveKey(password, macKeyDerivationSalt);
              console.log("Derived MAC key.");

              // already in hex
              var encryptedPrivateKey = aegisUtils.encryptAES(encKey, encIv,
                forge.pki.privateKeyToPem(privateKey));
              console.log("Encrypted the private key.");

              var publicKeyHMAC = aegisUtils.hmac(macKey, me.publicKey); // already in hex
              console.log("HMACed the public key.");

              var message = {
                hashedMasterPassword: hashedMasterPassword,
                masterPasswordSalt: masterPasswordSalt.hex,
                encKeyDerivationSalt: encKeyDerivationSalt.hex,
                macKeyDerivationSalt: macKeyDerivationSalt.hex,
                publicKeyMAC: publicKeyHMAC, // already in hex
                encIv: encIv.hex,
                privateKey: encryptedPrivateKey, // already in hex
              };
              $http.put('/users/master', JSON.stringify(message)).then(function (response) {
                if (response.status == 200) {
                  $location.path(routeToPageOnSuccess);
                } else {
                  console.log('Sending master password change request to server failed.');
                  if (execOnError) {
                    execOnError(response);
                  }
                }
              }, execOnError);
            } else {
              console.log(FAIL_USER_RETRIEVAL_MESSAGE);
              if (execOnError) {
                execOnError(response);
              }
            }

          }, execOnError);
        }

        // function definitions end here
      };
    }
  ]);