var mongodb = require('mongodb');
var objectID = mongodb.ObjectID;
var crypto = require('crypto');
var express = require('express');
var bodyParser = require('body-parser');

var genRandomString = function(len) {
    return crypto.randomBytes(Math.ceil(len/2))
        .toString('hex')
        .slice(0, len);
};

var sha512 = function(password, salt) {
    var hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    var value = hash.digest('hex');
    return {
        salt : salt,
        passwordHash: value
    };
};
function saltHashPassword(userPassword) {
    var salt = genRandomString(16);
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function checkHashPassword(userPassword, salt) {
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

var MongoClient = mongodb.MongoClient;
var url = 'mongodb://localhost:27017';
MongoClient.connect(url,{useNewUrlParser: true}, function(err, client) {
    if(err) {
        console.log('Unable to connect to mongoDb Server Error: ', err);
    } else {
        //Register
        app.post('/register', (req, response, next) =>  {
            var post_data = req.body;
            var plain_password = post_data.password;
            var hash_data = saltHashPassword(plain_password);
            var password = hash_data.passwordHash;
            var salt = hash_data.salt;
            var name = post_data.name;
            var email = post_data.email;

            var insertJson = {
                'email' : email,
                'password' : password,
                'salt': salt,
                'name':name
            };
            var db = client.db('androidwithnodejs');
            db.collection('user')
                .find({'email': email})
                .count(function(err, number) {
                    if(number!=0) {
                        response.json('Email already exists.');
                        console.log('Email already exists.');
                    }
                    else {
                        db.collection('user')
                            .insertOne(insertJson, function(err, res) {
                                response.json('Registration success.');
                                console.log('Registration success.');
                            });
                    }
                });
        });

        app.post('/login', (req, res, next) => {
            var post_data = req.body;
            var userPassword = post_data.password;
            var email = post_data.email;

            var db = client.db('androidwithnodejs');
            db.collection('user')
                .find({'email': email})
                .count(function(err, number) {
                    if(number == 0) {
                        res.json('Email not exists.');
                        console.log('Email not exists.');
                    }
                    else {
                        db.collection('user')
                            .findOne({'email': email}, function(err, user) {
                                var salt = user.salt;
                                var hashedPassword = checkHashPassword(userPassword, salt).passwordHash;
                                var encryptedPassword = user.password;
                                if(hashedPassword == encryptedPassword){
                                    res.json('Login Success');
                                    console.log('Login success');
                                }
                                else {
                                    res.json('Wrong Password');
                                    console.log('Wrong Password');
                                }
                            });
                    }
                });
            
        });
          app.listen(3000, ()=> {
              console.log('Connected to MongoDb server, Webservice running on port 3000');
          });
    }
});
 