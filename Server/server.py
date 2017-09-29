import BaseHTTPServer, ssl
import SimpleHTTPServer
import SocketServer
import os.path
from BaseHTTPServer import BaseHTTPRequestHandler,HTTPServer
import math
import json
import MySQLdb
import datetime
import time
import jwt
import os

dbuser = ''
dbpass = ''
dbname = 'myairbnb'
JWTsecret = "nZr4u7x!z%C*F-JaN,RgUkXp2s5v8yfUjXn2r5u8x/A?B$D(G+KaFdSgVkYp3s@M"
PATHuser = '.\public\data\images\Users'
PATHroom = '.\public\data\images\Rooms'

if(not os.path.exists(PATHuser)):
	os.makedirs(PATHuser)

if(not os.path.exists(PATHroom)):
	os.makedirs(PATHroom)

def createDB(db_false, cur_false):
	try:
		createSchema = """CREATE SCHEMA """+dbname
		cur_false.execute(createSchema)
		db_false.commit()
		print 'Schema '+dbname+' created...\n'
		cur_false.close()
		db_false.close()
		db = MySQLdb.connect(host = "localhost", user = dbuser, passwd = dbpass, db = dbname)  
		cur = db.cursor()
		createTable = """CREATE TABLE hostrates (idhostRates INT NOT NULL AUTO_INCREMENT, `from` VARCHAR(45) NOT NULL, `to` VARCHAR(45) NOT NULL, roomName VARCHAR(45) NOT NULL, rate LONGTEXT NOT NULL, PRIMARY KEY (idhostRates))"""
		cur.execute(createTable)
		db.commit()
		print 'Table hostrates created...'
		print '+-------------+------+----+----------+------+'
		print '| idhostRates | from | to | roomName | rate |'
		print '+-------------+------+----+----------+------+\n'
		createTable = """CREATE TABLE messages (messageID INT NOT NULL AUTO_INCREMENT, `from` VARCHAR(45) NOT NULL, `to` VARCHAR(45) NOT NULL, message MEDIUMTEXT NOT NULL, unread VARCHAR(3) NOT NULL, PRIMARY KEY (messageID))"""
		cur.execute(createTable)
		db.commit()
		print 'Table messages created...'
		print '+-----------+------+----+---------+--------+'
		print '| messageID | from | to | message | unread |'
		print '+-----------+------+----+---------+--------+\n'
		createTable = """CREATE TABLE reservations (reservationID INT NOT NULL AUTO_INCREMENT, username VARCHAR(45) NOT NULL, hostName VARCHAR(45) NOT NULL, roomName VARCHAR(45) NOT NULL, dateFrom DATE NOT NULL, dateTo DATE NOT NULL, rated VARCHAR(3) NOT NULL, PRIMARY KEY (reservationID))"""
		cur.execute(createTable)
		db.commit()
		print 'Table reservations created...'
		print '+---------------+----------+----------+----------+----------+--------+-------+'
		print '| reservationID | username | hostName | roomName | dateFrom | dateTo | rated |'
		print '+---------------+----------+----------+----------+----------+--------+-------+\n'
		createTable = """CREATE TABLE rooms (roomName VARCHAR(45) NOT NULL, hostName VARCHAR(45) NOT NULL, country VARCHAR(45) NOT NULL, city VARCHAR(45) NOT NULL, address VARCHAR(45) NOT NULL, dateFrom DATE NOT NULL, dateTo DATE NOT NULL, maxVisitors INT(11) NOT NULL, minPrice VARCHAR(45) NOT NULL, roomType VARCHAR(45) NOT NULL, rules VARCHAR(45) NOT NULL, description MEDIUMTEXT NOT NULL, area VARCHAR(45) NOT NULL, rate DECIMAL(2,0) NOT NULL, peopleRated INT(11) NOT NULL, images LONGTEXT, PRIMARY KEY (roomName, hostName))"""
		cur.execute(createTable)
		db.commit()
		print 'Table rooms created...'
		print '+----------+----------+---------+------+---------+----------+--------+-------------+----------+----------+-------+-------------+------+------+'
		print '| roomName | hostName | country | city | address | dateFrom | dateTo | maxVisitors | minPrice | roomType | rules | description | area | rate |'
		print '+----------+----------+---------+------+---------+----------+--------+-------------+----------+----------+-------+-------------+------+------+\n'
		createTable = """CREATE TABLE search (searchID INT NOT NULL AUTO_INCREMENT, username VARCHAR(45) NOT NULL, country VARCHAR(45) NOT NULL, PRIMARY KEY (searchID))"""
		cur.execute(createTable)
		db.commit()
		print 'Table search created...'
		print '+----------+----------+---------+'
		print '| searchID | username | country |'
		print '+----------+----------+---------+\n'
		createTable = """CREATE TABLE users (userID INT NOT NULL AUTO_INCREMENT, username VARCHAR(45) NOT NULL, password VARCHAR(45) NOT NULL, firstName VARCHAR(45) NOT NULL, lastName VARCHAR(45) NOT NULL, telephone VARCHAR(45) NOT NULL, email VARCHAR(45) NOT NULL, host VARCHAR(3) NOT NULL, userImage LONGTEXT NOT NULL, registrationDate TIMESTAMP NOT NULL, UNIQUE INDEX (username), UNIQUE INDEX(email), PRIMARY KEY (userID))"""
		cur.execute(createTable)
		db.commit()
		print 'Table users created...'
		print '+--------+----------+----------+-----------+----------+-----------+-------+------+-----------+------------------+'
		print '| userID | username | password | firstName | lastName | telephone | email | host | userImage | registrationDate |'
		print '+--------+----------+----------+-----------+----------+-----------+-------+------+-----------+------------------+\n'
		cur.close()
		db.close()
	except (MySQLdb.Error, MySQLdb.Warning) as e:
		print e
		cur.close()
		db.close()
	return

def checkDB():
	db = MySQLdb.connect(host = "127.0.0.1", user = dbuser, passwd = dbpass)  
	cur = db.cursor()
	checkSchema = """SELECT * FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='"""+dbname+"""'"""
	cur.execute(checkSchema)
	row = cur.fetchone()
	if(not row):
		createDB(db, cur)
	else:
		print 'Schema '+dbname+' already exists...'
	return

def isHost(host):
	if (host):
		return '1'
	else:
		return '0'

def getHost(host):
	if (host == '0'):
		return 'false'
	else:
		return 'true'

def CapitalizeFirst(string):
    return string[0].upper() + string[1:]

def DATE(string):
	day, month, year = string.split("/")
	day = int(float(day))
	month = int(float(month))
	year = int(float(year))
	return datetime.date(year, month, day)

def SYSTEM_DATE():
	month, day, year = time.strftime("%x").split("/")
	day = int(float(day))
	month = int(float(month))
	year = int(float(year))
	return datetime.date(year+2000, month, day)

def findImage(imageFile):
	file = open(imageFile,'r')
	image = file.read()
	file.close()
	return image

def validateAndRefreshToken(token, username):
	try:
		decodedJWT = jwt.decode(token, JWTsecret, algorithms=['HS256'])
		return jwt.encode({"username": decodedJWT['username'], 'exp': datetime.datetime.utcnow() + datetime.timedelta(seconds = 3600)}, JWTsecret, algorithm="HS256")
	except jwt.InvalidTokenError as e:
		print e
		return

def getUsername(token):
	decodedJWT = jwt.decode(token, JWTsecret, algorithms=['HS256'])
	return decodedJWT['username']

def make_request_handler_class():
	class MyRequestHandler(BaseHTTPRequestHandler):
		
		def do_POST(self):
			self.send_response(200)
			self.send_header('Content-type','application/json')
			self.end_headers()
			db = MySQLdb.connect(host = "localhost", user = dbuser, passwd = dbpass, db = dbname)  
			cur = db.cursor()	

			if self.path == "/checkJWT":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				token = validateAndRefreshToken(dataReceived['jwt'], dataReceived['username'])
				if(token):
					self.wfile.write(unicode('{"res": "Valid", "jwt": "'+token+'"}'))
				else:
					self.wfile.write(unicode('{"res": "Invalid"}'))

			if self.path == "/signUp":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				file = open(os.path.join(PATHuser, dataReceived['username']),'w')
				file.write(dataReceived['userImage'])
				file.close()
				try:
					dbQuery = """INSERT INTO users (username, password, firstName, lastName, email, telephone, host, userImage) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"""
					cur.execute(dbQuery, (dataReceived['username'], dataReceived['password'], dataReceived['firstName'], dataReceived['lastName'], dataReceived['email'], dataReceived['telephone'], isHost(dataReceived['host']), file.name))
					db.commit()		
					cur.close()
					db.close()
					dataReceived["res"] = "valid"
					dataReceived["jwt"] = jwt.encode({'username': dataReceived['username'], 'exp': datetime.datetime.utcnow() + datetime.timedelta(seconds = 3600)}, JWTsecret, algorithm='HS256')
					self.wfile.write(unicode(json.dumps(dataReceived)))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					error_msg = '{"res":"error","msg":"username or e-mail already exists"}'
					self.wfile.write(unicode(error_msg))
				return 

			if self.path=="/signIn":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				if(dataReceived['jwt'] != ""):
					dbQuery = """SELECT * FROM users WHERE username=%s LIMIT 1"""
					cur.execute(dbQuery, [getUsername(dataReceived['jwt'])])
					dataReceived['username'] = getUsername(dataReceived['jwt'])
				else:
					dbQuery = """SELECT * FROM users WHERE username=%s AND password=%s LIMIT 1"""
					cur.execute(dbQuery, (dataReceived['username'], dataReceived['password']))
				row = cur.fetchone()
				db.commit()
				cur.close()
				db.close()
				if (not row):
					error_msg = '{"res":"error","msg":"Incorrect Username or Password"}'
					self.wfile.write(unicode(error_msg))
				else:
					file = open(row[8],'r')
					self.wfile.write(unicode('{ "username": "'+row[1]+'", "password": "'+row[2]+'", "firstName": "'+row[3]+'", "lastName": "'+row[4]+'", "telephone": "'+row[5]+'", "email": "'+row[6]+'", "host": '+getHost(row[7])+', "userImage": "'+file.read()+'", "res": "valid", "jwt": "'+jwt.encode({'username': dataReceived['username'], 'exp': datetime.datetime.utcnow() + datetime.timedelta(seconds = 3600)}, JWTsecret, algorithm='HS256')+'" }'))
					file.close()
				return

			if self.path == "/updateUser":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				if (os.path.isfile(os.path.join(PATHuser, dataReceived['username']))):
					os.remove(os.path.join(PATHuser, dataReceived['username']))
				file = open(os.path.join(PATHuser, dataReceived['username']),'w')
				file.write(dataReceived['userImage'])
				file.close()
				try:
					dbQuery = """UPDATE users SET password=%s, firstName=%s, lastName=%s, email=%s, telephone=%s, host=%s, userimage=%s WHERE username=%s"""
					cur.execute(dbQuery, (dataReceived['password'], dataReceived['firstName'], dataReceived['lastName'], dataReceived['email'], dataReceived['telephone'], isHost(dataReceived['host']), file.name, dataReceived['username']))
					db.commit()		
					cur.close()
					db.close()
					dataReceived['res'] = 'valid'
					self.wfile.write(unicode(json.dumps(dataReceived)))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					error_msg = '{"res":"error","msg":"Invalid email or email already exists"}'
					self.wfile.write(unicode(error_msg))
				return

			if self.path == "/returnRooms":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				if (dataReceived['type'] == 'HOST_ROOMS'):
					dataReceived['hostName']
					try:
						dbQuery = """SELECT roomName, description, images, rate FROM rooms WHERE hostName=%s"""
						cur.execute(dbQuery, [dataReceived['hostName']])
						rows = cur.fetchall()
						jsonArray = {}
						fileNames = []
						jsonSend = {}
						jsonSend['names'] = []
						jsonSend['images'] = {}
						jsonSend['descriptions'] = []
						jsonSend['rates'] = []
						i = 0
						for row in rows :
							jsonArray = json.loads(row[2])
							jsonSend['names'].append(row[0])
							jsonSend['descriptions'].append(row[1])
							fileNames = jsonArray['fileNames']
							jsonSend['images'][str(i)] = findImage(fileNames[0])
							i += 1
							jsonSend['rates'].append(str(row[3]))
						db.commit()		
						cur.close()
						db.close()
						if(i == 0):
							jsonSend['res'] = 'error'
							jsonSend['msg'] = 'You have no rooms'
						else:
							jsonSend['res'] = 'success'
						self.wfile.write(unicode(json.dumps(jsonSend)))
					except (MySQLdb.Error, MySQLdb.Warning) as e:
						print e		
				if (dataReceived['type'] == 'ONE'):
					try:
						dbQuery = """SELECT roomName, hostName, country, city, address, dateFrom, dateTo, maxVisitors, minPrice, roomType, rules, description, area, rate, peopleRated, images FROM rooms WHERE roomName=%s"""
						cur.execute(dbQuery, [dataReceived['roomName']])
						row = cur.fetchone()
						dbQuery = """SELECT * FROM reservations WHERE username=%s AND roomName=%s AND dateTo<%s AND rated=%s LIMIT 1"""
						cur.execute(dbQuery, (getUsername(dataReceived['jwt']), dataReceived['roomName'], SYSTEM_DATE(), '0'))
						row1 = cur.fetchone()
						if(row1):
							visited = '1'
						else:
							visited = '0'
						db.commit()		
						cur.close()
						db.close()
						jsonSend = {}
						jsonSend = json.loads(row[15])
						fileNames = []
						fileNames = jsonSend['fileNames']
						images = []
						for i in range(0, len(fileNames)):
							images.append(findImage(fileNames[i]))
						y, m, d = str(row[5]).split('-')
						dateFrom = d +'/'+ m +'/'+ y
						y, m, d = str(row[6]).split('-')
						dateTo = d +'/'+ m +'/'+ y
						jsonString = '{ "roomName": "'+row[0]+'", "hostName": "'+row[1]+'", "country": "'+row[2]+'", "city": "'+row[3]+'",  "address": "'+row[4]+'", "dateFrom": "'+dateFrom+'", "dateTo": "'+dateTo+'", "maxVisitors": "'+str(row[7])+'", "minPrice": "'+row[8]+'", "roomType": "'+row[9]+'", "rules": "'+row[10]+'", "description": "'+row[11]+'", "area": "'+row[12]+'", "rate": "'+str(row[13])+'",  "peopleRated": "'+str(row[14])+'", "visited": "'+visited+'"}'
						jsonSend = {}
						jsonSend = json.loads(jsonString.replace('\n', '\\n'))
						jsonSend['images'] = []
						jsonSend['images'] = images
						self.wfile.write(unicode(json.dumps(jsonSend)))
					except (MySQLdb.Error, MySQLdb.Warning) as e:
						print e
				if (dataReceived['type'] == 'FAMOUS'):
					try:
						dbQuery = """SELECT country, COUNT(country) AS `country_occurance` FROM search WHERE username=%s GROUP BY country ORDER BY `country_occurance` DESC LIMIT 1"""
						cur.execute(dbQuery, [getUsername(dataReceived['jwt'])])
						countrySearch = cur.fetchone()
						if (countrySearch):
							dbQuery = """SELECT roomName, minPrice, address, images, rate FROM rooms WHERE country=%s ORDER BY rate DESC LIMIT 20"""
							cur.execute(dbQuery, [countrySearch[0]])
						else:	
							dbQuery = """SELECT roomName, minPrice, address, images, rate FROM rooms ORDER BY rate DESC LIMIT 20"""
							cur.execute(dbQuery,())
						rows = cur.fetchall()
						jsonArray = {}
						fileNames = []
						jsonSend = {}
						jsonSend['names'] = []
						jsonSend['images'] = {}
						jsonSend['descriptions'] = []
						jsonSend['prices'] = []
						jsonSend['addresses'] = []
						jsonSend['rates'] = []
						i = 0
						for row in rows :
							jsonArray = json.loads(row[3])
							jsonSend['names'].append(row[0])
							jsonSend['prices'].append(row[1])
							jsonSend['addresses'].append(row[2])
							fileNames = jsonArray['fileNames']
							jsonSend['images'][str(i)] = findImage(fileNames[0])
							jsonSend['rates'].append(str(row[4]))
							i+=1
						db.commit()		
						cur.close()
						db.close()
						self.wfile.write(unicode(json.dumps(jsonSend)))
					except (MySQLdb.Error, MySQLdb.Warning) as e:
						print e
				return

			if self.path == "/addRoom":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				dbQuery = """SELECT * FROM rooms WHERE roomName=%s AND hostname=%s AND country=%s AND city=%s AND address=%s LIMIT 1"""
				cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName'], dataReceived['country'], dataReceived['city'], dataReceived['address']))
				rows = cur.fetchall()
				if (len(rows) == 0):
					if(not os.path.exists(os.path.join(PATHroom, dataReceived['hostName']))):
						os.mkdir(os.path.join(PATHroom, dataReceived['hostName']))
					if(not os.path.exists(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))):
						os.mkdir(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))
					jsonArray = {}
					jsonArray['fileNames'] = []
					for i in range(0, len(dataReceived['names'])):
						name, x = dataReceived['names'][i].split(".")
						name = name.split("/")
						file = open(os.path.join(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']), name[-1]),'w')
						file.write(dataReceived['images'][i])
						jsonArray['fileNames'].append(file.name)
						file.close()
					try:
						dbQuery = """INSERT INTO rooms (roomName, hostName, country, city, address, dateFrom, dateTo, maxVisitors, minPrice, roomType, rules, description, area, rate, peopleRated, images) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
						cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName'], dataReceived['country'], dataReceived['city'], dataReceived['address'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo']), dataReceived['maxVisitors'], dataReceived['minPrice'], dataReceived['roomType'], dataReceived['rules'], dataReceived['description'], dataReceived['area'], dataReceived['rate'], '0', json.dumps(jsonArray)))
						db.commit()		
						cur.close()
						db.close()
						self.wfile.write(unicode('{"res":"success","msg":"Room successfully inserted!"}'))
					except (MySQLdb.Error, MySQLdb.Warning) as e:
						print e
						self.wfile.write(unicode('{"res":"error","msg":"Something went wrong! Room not inserted"}'))
				else:
					self.wfile.write(unicode('{"res":"error","msg":"Room already exist!"}'))
				return

			if self.path == "/updateRoom":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				if(not os.path.exists(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))):
					os.mkdir(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))
				else:
					fileList = os.listdir(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))
					home = os.getcwd()
					os.chdir(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))
					for f in fileList:
						os.remove(f)
					os.chdir(home)
				jsonArray = {}
				jsonArray['fileNames'] = []
				for i in range(0, len(dataReceived['names'])):
					name, x = dataReceived['names'][i].split(".")
					name = name.split("/")
					file = open(os.path.join(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']), name[-1]),'w')
					file.write(dataReceived['images'][i])
					jsonArray['fileNames'].append(file.name)
					file.close()
				try:
					dbQuery = """UPDATE rooms SET roomName=%s, country=%s, city=%s, address=%s, dateFrom=%s, dateTo=%s, maxVisitors=%s, minPrice=%s, roomType=%s, rules=%s, description=%s, area=%s, images=%s WHERE roomName=%s AND hostName=%s"""
					cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['country'], dataReceived['city'], dataReceived['address'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo']), dataReceived['maxVisitors'], dataReceived['minPrice'], dataReceived['roomType'], dataReceived['rules'], dataReceived['description'], dataReceived['area'], json.dumps(jsonArray), dataReceived['roomName'], dataReceived['hostName']))
					db.commit()		
					cur.close()
					db.close()
					self.wfile.write(unicode('{"res":"success","msg":"Room successfully updated!"}'))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
					self.wfile.write(unicode('{"res":"error","msg":"Something went wrong! Room not updated"}'))
				return

			if self.path == "/deleteRoom":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """SELECT address FROM rooms WHERE roomName=%s AND hostName=%s"""
					cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName']))
					address = cur.fetchone()
					dbQuery = """DELETE FROM rooms WHERE roomName=%s AND hostName=%s"""
					cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName']))
					db.commit()		
					dbQuery = """DELETE FROM reservations WHERE roomName=%s AND hostName=%s"""
					cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName']))
					cur.close()
					db.close()
					fileList = os.listdir(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))
					home = os.getcwd()
					os.chdir(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))
					for f in fileList:
						os.remove(f)
					os.chdir(home)
					if(not os.listdir(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))):
						os.rmdir(os.path.join(os.path.join(PATHroom, dataReceived['hostName']), dataReceived['roomName']))
					if(not os.listdir(os.path.join(PATHroom, dataReceived['hostName']))):
						os.rmdir(os.path.join(PATHroom, dataReceived['hostName']))
					self.wfile.write(unicode('{"res":"success","msg":"Room successfully deleted!"}'))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
					self.wfile.write(unicode('{"res":"error","msg":"Something went wrong! Room was not deleted"}'))
				return

			if self.path == "/fetchMessages":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """SELECT * FROM messages WHERE `to`=%s ORDER BY `from`"""
					cur.execute(dbQuery, [dataReceived['username']])
					rows = cur.fetchall()
					jsonSend = {}
					jsonSend['from'] = []
					jsonSend['messages'] = []
					jsonSend['images'] = []
					for row in rows :
						jsonSend['from'].append(row[1])
						jsonSend['messages'].append(row[3])
						if(len(jsonSend['from']) >= 2):
							if(row[1] != jsonSend['from'][-2]):
								dbQuery = """SELECT userImage FROM users WHERE username=%s"""
								cur.execute(dbQuery, [jsonSend['from'][-1]])
								image = cur.fetchone()
								if(image):
									jsonSend['images'].append(findImage(image[0]))
								else:
									jsonSend['images'].append("")
							else:
								jsonSend['images'].append(jsonSend['images'][-1])
						else:
							dbQuery = """SELECT userImage FROM users WHERE username=%s"""
							cur.execute(dbQuery, [jsonSend['from'][-1]])
							image = cur.fetchone()
							if(image):
								jsonSend['images'].append(findImage(image[0]))
							else:
								jsonSend['images'].append("")
					db.commit()		
					cur.close()
					db.close()
					self.wfile.write(unicode(json.dumps(jsonSend)))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
				return

			if self.path == "/msgForward":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """INSERT INTO messages (`from`, `to`, message, unread) VALUES (%s, %s, %s, %s)"""
					cur.execute(dbQuery, (dataReceived['from'], dataReceived['to'], dataReceived['message'], '1'))
					db.commit()
					cur.close()
					db.close()
					self.wfile.write(unicode('{"res":"success","msg":"Message sent!"}'))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
					self.wfile.write(unicode('{"res":"error","msg":"Error. Message was not sent!"}'))
				return

			if self.path == "/msgDelete":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """DELETE FROM messages WHERE `from`=%s AND `to`=%s AND message=%s"""
					cur.execute(dbQuery, (dataReceived['from'], dataReceived['to'], dataReceived['message']))
					db.commit()
					cur.close()
					db.close()
					self.wfile.write(unicode('{"res":"success","msg":"Message deleted!"}'))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
					self.wfile.write(unicode('{"res":"error","msg":"Error. Message was not deleted!"}'))
				return

			if self.path == "/getImage":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """SELECT userImage FROM users WHERE username=%s LIMIT 1"""
					cur.execute(dbQuery, [dataReceived['hostName']])
					row = cur.fetchone()
					db.commit()		
					cur.close()
					db.close()
					jsonSend = {}
					jsonSend['image'] = findImage(row[0])
					self.wfile.write(unicode(json.dumps(jsonSend)))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e				
				return

			if self.path == "/hostRoomInfo":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """SELECT rate, `from` FROM hostrates WHERE `to`=%s AND roomName=%s"""
					cur.execute(dbQuery, (dataReceived['hostName'], dataReceived['roomName']))
					rows = cur.fetchall()
					jsonSend = {}
					jsonSend['rates'] = []
					jsonSend['from'] = []
					jsonSend['images'] = []
					for row in rows:
						jsonSend['rates'].append(row[0])
						jsonSend['from'].append(row[1])
						dbQuery = """SELECT userImage FROM users WHERE username=%s LIMIT 1"""
						cur.execute(dbQuery, [jsonSend['from'][-1]])
						image = cur.fetchone()
						if(image):
							jsonSend['images'].append(findImage(image[0]))
						else:
							jsonSend['images'].append("")
					dbQuery = """SELECT firstName, lastName, userImage, telephone FROM users WHERE username=%s LIMIT 1"""
					cur.execute(dbQuery, [dataReceived['hostName']])
					row = cur.fetchone()
					jsonSend['firstName'] = row[0]
					jsonSend['lastName'] = row[1]
					jsonSend['userImage'] = findImage(row[2])
					jsonSend['telephone'] = row[3]
					db.commit()
					cur.close()
					db.close()
					self.wfile.write(unicode(json.dumps(jsonSend)))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
				return

			if self.path=="/search":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					if(dataReceived['type'] == 'simple'):	
						dbQuery = """SELECT roomName, minPrice, address, images, rate, hostName FROM rooms WHERE maxVisitors>=%s AND %s>=dateFrom AND %s<=dateTo ORDER BY minPrice"""
						cur.execute(dbQuery, (dataReceived['maxVisitors'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo'])))
					elif(dataReceived['type'] == 'country'):
						dbQuery = """SELECT roomName, minPrice, address, images, rate, hostName FROM rooms WHERE country=%s AND maxVisitors>=%s AND %s>=dateFrom AND %s<=dateTo ORDER BY minPrice"""
						cur.execute(dbQuery, (dataReceived['country'], dataReceived['maxVisitors'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo'])))
					elif(dataReceived['type'] == 'countryCity'):	
						dbQuery = """SELECT roomName, minPrice, address, images, rate, hostName FROM rooms WHERE country=%s AND city=%s AND maxVisitors>=%s AND %s>=dateFrom AND %s<=dateTo ORDER BY minPrice"""
						cur.execute(dbQuery, (dataReceived['country'], dataReceived['city'], dataReceived['maxVisitors'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo'])))
					elif(dataReceived['type'] == 'countryCityAddress'):	
						dbQuery = """SELECT roomName, minPrice, address, images, rate, hostName FROM rooms WHERE country=%s AND city=%s AND address=%s AND maxVisitors>=%s AND %s>=dateFrom AND %s<=dateTo ORDER BY minPrice"""
						cur.execute(dbQuery, (dataReceived['country'], dataReceived['city'], dataReceived['address'], dataReceived['maxVisitors'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo'])))
					rows = cur.fetchall()
					if(dataReceived['country'] != ''):
						dbQuery = """INSERT INTO search (username, country) VALUES (%s, %s)"""
						cur.execute(dbQuery, (getUsername(dataReceived['jwt']), dataReceived['country']))
					jsonArray = {}
					fileNames = []
					jsonSend = {}
					jsonSend['names'] = []
					jsonSend['images'] = {}
					jsonSend['prices'] = []
					jsonSend['addresses'] = []
					jsonSend['rates'] = []
					jsonSend['visitors'] = dataReceived['maxVisitors']
					jsonSend['res'] = 'valid'
					count = 0
					for row in rows:
						dbQuery = """SELECT * FROM reservations WHERE roomName=%s AND hostName=%s  AND ((%s>=dateFrom AND %s<dateTo) OR (%s>=dateFrom AND %s<=dateTo) OR (%s<=dateFrom AND %s>=dateTo)) LIMIT 1"""
						cur.execute(dbQuery, (row[0], row[5], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo']), DATE(dataReceived['dateTo']), DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo'])))
						exists = cur.fetchone()
						if(not exists):
							jsonSend['names'].append(row[0])
							jsonSend['prices'].append(row[1])
							jsonSend['addresses'].append(row[2])
							jsonArray = json.loads(row[3])
							fileNames = jsonArray['fileNames']
							jsonSend['images'][str(count)] = findImage(fileNames[0])
							jsonSend['rates'].append(str(row[4]))
							count += 1
						else:
							exists = None
					db.commit()		
					cur.close()
					db.close()
					jsonSend['dateFrom'] = dataReceived['dateFrom']
					jsonSend['dateTo'] = dataReceived['dateTo']
					if (not rows):
						self.wfile.write(unicode('{"res":"error","msg":"No results found"}'))
					else:
						if(count <= 50):
							jsonSend['msg'] = str(count) + ' results found'
						else:
							jsonSend['msg'] = 'More than 50 results found'
						self.wfile.write(unicode(json.dumps(jsonSend)))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
				return	

			if self.path == "/checkAvailability":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """SELECT * FROM reservations WHERE roomName=%s AND hostName=%s AND ((%s>=dateFrom AND %s<dateTo) OR (%s>=dateFrom AND %s<=dateTo) OR (%s<=dateFrom AND %s>=dateTo)) LIMIT 1"""
					cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo']), DATE(dataReceived['dateTo']), DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo'])))
					row = cur.fetchone()
					if(not row):
						dbQuery = """SELECT * FROM rooms WHERE %s>=dateFrom AND %s<=dateTo"""
						cur.execute(dbQuery, (DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo'])))
						exists = cur.fetchone()
						if(exists):
							self.wfile.write(unicode('{"res":"success","msg":"Checked"}'))
						else:
							self.wfile.write(unicode('{"res":"error","msg":"Sorry! The room is not available at given dates!"}'))
					else:
						self.wfile.write(unicode('{"res":"error","msg":"Sorry! The room is not available at given dates!"}'))
					db.commit()		
					cur.close()
					db.close()
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
				return

			if self.path == "/addReservation":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """INSERT INTO reservations (username, hostName, roomName, dateFrom, dateTo, rated) VALUES (%s, %s, %s, %s, %s, %s)"""
					cur.execute(dbQuery, (dataReceived['username'], dataReceived['hostName'], dataReceived['roomName'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo']), '0'))
					row = cur.fetchone()
					db.commit()		
					cur.close()
					db.close()
					self.wfile.write(unicode('{"res":"success","msg":"Reservation successfully made!"}'))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
				return

			if self.path == "/rateRoom":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """SELECT rate, peopleRated FROM rooms WHERE roomName=%s AND hostName=%s"""
					cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName']))
					row = cur.fetchone()
					dbQuery = """UPDATE rooms SET peopleRated=%s, rate=%s WHERE roomName=%s AND hostName=%s"""
					decimal = row[0] * row[1]
					rate = decimal + dataReceived['rate']
					rate = rate / (row[1] + 1)
					cur.execute(dbQuery, (row[1] + 1, rate, dataReceived['roomName'], dataReceived['hostName']))
					db.commit()		
					dbQuery = """UPDATE reservations SET rated=%s WHERE roomName=%s AND hostName=%s"""
					cur.execute(dbQuery, ('1', dataReceived['roomName'], dataReceived['hostName']))
					db.commit()
					if(dataReceived['rateText'] != ""):
						dbQuery = """INSERT INTO hostRates (`from`, `to`, roomName, rate) values (%s, %s, %s, %s)"""
						cur.execute(dbQuery, (dataReceived['from'], dataReceived['hostName'], dataReceived['roomName'], dataReceived['rateText']))
						db.commit()
					cur.close()
					db.close()
					self.wfile.write(unicode('{"peopleRated": "'+str(row[1]+1)+'", "rate": "'+str(rate)+'"}'))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e				
				return

			#At MainScreenHost when Reservations selected only fututre reservetions must be shown
			if self.path == "/fetchReservations":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """SELECT username, dateFrom, dateTo, roomName FROM reservations WHERE roomName=%s AND hostName=%s"""
					cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName']))
					rows = cur.fetchall()
					jsonSend = {}
					jsonSend['username'] = []
					jsonSend['dateFrom'] = []
					jsonSend['dateTo'] = []
					for row in rows:
						jsonSend['username'].append(row[0])
						y, m, d = str(row[1]).split('-')
						jsonSend['dateFrom'].append(d +'/'+ m +'/'+ y)
						y, m, d = str(row[2]).split('-')
						jsonSend['dateTo'].append(d +'/'+ m +'/'+ y)
					if(rows):
						jsonSend['roomName'] = rows[0][3]
						jsonSend['res'] = 'success'
						self.wfile.write(unicode(json.dumps(jsonSend)))
					else:
						self.wfile.write(unicode('{"res":"error","msg":"There are no reservations for this room"}'))
					db.commit()		
					cur.close()
					db.close()
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
				return

			if self.path == "/deleteReservation":
				dataReceived = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
				try:
					dbQuery = """DELETE FROM reservations WHERE roomName=%s AND hostName=%s AND username=%s AND dateFrom=%s AND dateTo=%s"""
					cur.execute(dbQuery, (dataReceived['roomName'], dataReceived['hostName'], dataReceived['username'], DATE(dataReceived['dateFrom']), DATE(dataReceived['dateTo'])))
					db.commit()
					cur.close()
					db.close()
					self.wfile.write(unicode('{"res":"success","msg":"Reservation successfully deleted!", "to": "'+dataReceived['username']+'"}'))
				except (MySQLdb.Error, MySQLdb.Warning) as e:
					print e
					self.wfile.write(unicode('{"res":"error","msg":"Something went wrong! Reservation was not deleted"}'))
				return

	return MyRequestHandler

checkDB()
host = ''
port = 4000
RequestHandler = make_request_handler_class()
server_address = (host, port)
httpd = BaseHTTPServer.HTTPServer(server_address, RequestHandler)
httpd.socket = ssl.wrap_socket(httpd.socket, server_side=True, certfile='key.pem', ssl_version=ssl.PROTOCOL_TLSv1)
print('Server Started...')		   
httpd.serve_forever()