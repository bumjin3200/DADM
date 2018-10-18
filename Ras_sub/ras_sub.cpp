//--- include
#include <stdlib.h>
#include <iostream>
#include <string>
#include <wiringPi.h>
#include <wiringSerial.h>
#include <math.h>
#include <unistd.h>
#include <thread> 
#include <stdio.h>
#include <linux/i2c-dev.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <termios.h>
#include <signal.h>
#include <string.h>
#include <sys/time.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include <bluetooth/rfcomm.h>

//--- define
#define MPU_ACCEL_XOUT1 0x3b
#define MPU_ACCEL_XOUT2 0x3c
#define MPU_ACCEL_YOUT1 0x3d
#define MPU_ACCEL_YOUT2 0x3e
#define MPU_ACCEL_ZOUT1 0x3f
#define MPU_ACCEL_ZOUT2 0x40
#define MPU_GYRO_XOUT1 0x43
#define MPU_GYRO_XOUT2 0x44
#define MPU_GYRO_YOUT1 0x45
#define MPU_GYRO_YOUT2 0x46
#define MPU_GYRO_ZOUT1 0x47
#define MPU_GYRO_ZOUT2 0x48
#define MPU_TEMP1 0x41
#define MPU_TEMP2 0x42
#define MPU_POWER1 0x6b
#define MPU_POWER2 0x6c

using namespace::std;
	
//----------grobal variables---------

int fd1;
int fd2;

char device1[] = "/dev/ttyACM0";
char device2[] = "/dev/ttyUSB0";

unsigned int _baud = 9600;

string result;
string toothresult;
string test[2];

double imdata[8] = {0,0,0,0,0,0,0,0};
int flag = 0;
int imflag = 0;
int check = 1;
int acc = 0;
int gesturesig = 0;
int disstate = 1;
int f1=0;
int client = 0;

//--------function--------
int _str2uuid( const char *uuid_str, uuid_t *uuid );
sdp_session_t *register_service(uint8_t rfcomm_channel);
int init_server();
char *read_server();
void write_server(char *message);
int setup();
string charstoString(char word);
double stringToDouble(string _str);
string* StringSplit(string strTarget, string strTok);
void viCom();
void bluetoothCom();
void senddata(int type, string number, string message);
void gestureCom();
void accCom();
void scope();

//----------fuction start----------

int main()
{
    //bluetooth socket connection
	client = init_server();
	int setup_data;
	//start serial connection
	setup_data = setup();
	if(setup_data <0)
		return 0;
    
    //start process
	thread t1(&viCom);
	thread t2(&bluetoothCom);
	thread t3(&gestureCom);
	thread t4(&accCom);
	thread t5(&scope);
	t1.join();
	t2.join();
	t3.join();
	t4.join();
	t5.join();
}

int _str2uuid( const char *uuid_str, uuid_t *uuid ) {

    uint32_t uuid_int[4];
    char *endptr;
 
    if( strlen( uuid_str ) == 36 ) {
        char buf[9] = { 0 };
 
        if( uuid_str[8] != '-' && uuid_str[13] != '-' &&
        uuid_str[18] != '-' && uuid_str[23] != '-' ) {
        return 0;
    }
    // first 8-bytes
    strncpy(buf, uuid_str, 8);
    uuid_int[0] = htonl( strtoul( buf, &endptr, 16 ) );
    if( endptr != buf + 8 ) return 0;
        // second 8-bytes
        strncpy(buf, uuid_str+9, 4);
        strncpy(buf+4, uuid_str+14, 4);
        uuid_int[1] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;
 
        // third 8-bytes
        strncpy(buf, uuid_str+19, 4);
        strncpy(buf+4, uuid_str+24, 4);
        uuid_int[2] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;
 
        // fourth 8-bytes
        strncpy(buf, uuid_str+28, 8);
        uuid_int[3] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;
 
        if( uuid != NULL ) sdp_uuid128_create( uuid, uuid_int );
    } else if ( strlen( uuid_str ) == 8 ) {
        // 32-bit reserved UUID
        uint32_t i = strtoul( uuid_str, &endptr, 16 );
        if( endptr != uuid_str + 8 ) return 0;
        if( uuid != NULL ) sdp_uuid32_create( uuid, i );
    } else if( strlen( uuid_str ) == 4 ) {
        // 16-bit reserved UUID
        int i = strtol( uuid_str, &endptr, 16 );
        if( endptr != uuid_str + 4 ) return 0;
        if( uuid != NULL ) sdp_uuid16_create( uuid, i );
    } else {
        return 0;
    }
 
    return 1;
 
}

sdp_session_t *register_service(uint8_t rfcomm_channel) {

    const char *service_name = "Armatus Bluetooth server";
    const char *svc_dsc = "A HERMIT server that interfaces with the Armatus Android app";
    const char *service_prov = "Armatus";
 
    uuid_t root_uuid, l2cap_uuid, rfcomm_uuid, svc_uuid,
           svc_class_uuid;
    sdp_list_t *l2cap_list = 0,
                *rfcomm_list = 0,
                 *root_list = 0,
                  *proto_list = 0,
                   *access_proto_list = 0,
                    *svc_class_list = 0,
                     *profile_list = 0;
    sdp_data_t *channel = 0;
    sdp_profile_desc_t profile;
    sdp_record_t record = { 0 };
    sdp_session_t *session = 0;
 
    // set the general service ID
    _str2uuid("00001101-0000-1000-8000-00805F9B34FB",&svc_uuid);
    sdp_set_service_id(&record, svc_uuid);
 
    char str[256] = "";
    sdp_uuid2strn(&svc_uuid, str, 256);
    printf("Registering UUID %s\n", str);
 
    // set the service class
    sdp_uuid16_create(&svc_class_uuid, SERIAL_PORT_SVCLASS_ID);
    svc_class_list = sdp_list_append(0, &svc_class_uuid);
    sdp_set_service_classes(&record, svc_class_list);
 
    // set the Bluetooth profile information
    sdp_uuid16_create(&profile.uuid, SERIAL_PORT_PROFILE_ID);
    profile.version = 0x0100;
    profile_list = sdp_list_append(0, &profile);
    sdp_set_profile_descs(&record, profile_list);
 
    // make the service record publicly browsable
    sdp_uuid16_create(&root_uuid, PUBLIC_BROWSE_GROUP);
    root_list = sdp_list_append(0, &root_uuid);
    sdp_set_browse_groups(&record, root_list);
 
    // set l2cap information
    sdp_uuid16_create(&l2cap_uuid, L2CAP_UUID);
    l2cap_list = sdp_list_append(0, &l2cap_uuid);
    proto_list = sdp_list_append(0, l2cap_list);
 
    // register the RFCOMM channel for RFCOMM sockets
    sdp_uuid16_create(&rfcomm_uuid, RFCOMM_UUID);
    channel = sdp_data_alloc(SDP_UINT8, &rfcomm_channel);
    rfcomm_list = sdp_list_append(0, &rfcomm_uuid);
    sdp_list_append(rfcomm_list, channel);
    sdp_list_append(proto_list, rfcomm_list);
 
    access_proto_list = sdp_list_append(0, proto_list);
    sdp_set_access_protos(&record, access_proto_list);
 
    // set the name, provider, and description
    sdp_set_info_attr(&record, service_name, service_prov, svc_dsc);
 
    // connect to the local SDP server, register the service record,
    // and disconnect
    session = sdp_connect(BDADDR_ANY, BDADDR_LOCAL, SDP_RETRY_IF_BUSY);
    sdp_record_register(session, &record, 0);
 
    // cleanup
    sdp_data_free(channel);
    sdp_list_free(l2cap_list, 0);
    sdp_list_free(rfcomm_list, 0);
    sdp_list_free(root_list, 0);
    sdp_list_free(access_proto_list, 0);
    sdp_list_free(svc_class_list, 0);
    sdp_list_free(profile_list, 0);
 
    return session;
}

int init_server() {
    int port = 3, result, sock, client_t, bytes_read, bytes_sent;
    struct sockaddr_rc loc_addr = { 0 }, rem_addr = { 0 };
    char buffer[1024] = { 0 };
    socklen_t opt = sizeof(rem_addr);
 
    // local bluetooth adapter
    loc_addr.rc_family = AF_BLUETOOTH;
    loc_addr.rc_bdaddr = *BDADDR_ANY;
    loc_addr.rc_channel = (uint8_t) port;
 
    // register service
    sdp_session_t *session = register_service(port);
    // allocate socket
    sock = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
    printf("socket() returned %d\n", sock);
 
    // bind socket to port 3 of the first available
    result = bind(sock, (struct sockaddr *)&loc_addr, sizeof(loc_addr));
    printf("bind() on channel %d returned %d\n", port, result);
 
    // put socket into listening mode
    result = listen(sock, 1);
    printf("listen() returned %d\n", result);
 
    //sdpRegisterL2cap(port);
 
    // accept one connection
    printf("calling accept()\n");
    client_t = accept(sock, (struct sockaddr *)&rem_addr, &opt);
    printf("accept() returned %d\n", client_t);
 
    ba2str(&rem_addr.rc_bdaddr, buffer);
    fprintf(stderr, "accepted connection from %s\n", buffer);
    memset(buffer, 0, sizeof(buffer));
 
    return client_t;
}
 
char *read_server() {
    // read data from the client
    char input[1024] = {0};
    int bytes_read;
    bytes_read = read(client, input, sizeof(input));
    if (bytes_read > 0) {
        fflush(stdin);
        printf("received [%s]\n", input);
        return input;
    } else {
        return "";
    }
	printf("end read_server");
}
 
void write_server(char *message) {
    // send data to the client
    char messageArr[1024] = { 0 };
    int bytes_sent;
    strcpy(messageArr, message);
 
    bytes_sent = write(client, messageArr, strlen(messageArr));
    if (bytes_sent > 0) {
        printf("sent [%s] %d\n", messageArr, bytes_sent);
    }
}

int setup()
{
	cin.clear();
	fd1 = serialOpen(device1, _baud);
	if (fd1 < 0)//ERROR001
	{
		cout << "Error001 : Unable to open serial device." << endl;
		return -1;
	}
	
	cin.clear();
	fd2 = serialOpen(device2, _baud);
	if (fd2 < 0)//ERROR001
	{
		cout << "Error001 : Unable to open serial device." << endl;
		return -1;
	}
	if (wiringPiSetup() == -1)//ERROR002
	{
		cout << "Error002 : Unable to start wiringPi" << endl;
		return -2;
	}
	cout << " setup succ" << endl;
}

// char -> String
string charstoString(char word)
{
	if (word != '\n')
		result = result + word;
	else{
	string temp = result;
	result = "";
	return temp;
		
	}
	return "";
}

double stringToDouble(string _str)
{
   int size, i, j;
   size = _str.size();
   int dot = _str.find('.');
   double result2 = 0;
   for (i = dot-1, j = 0; i >= 0; i--, j++)
   {
      result2 = result2 + (_str[i] - 48)*pow(10, j);
   }
   for (i = dot + 1, j = -1; i < size; i++, j--)
   {
      result2 = result2 + (_str[i] - 48)*pow(10, j);
   }
   return result2;
}


string* StringSplit(string strTarget, string strTok)
{
    int     nCutPos;
    int     nIndex     = 0;
    string* strResult = new string[3];
 
    while ((nCutPos = strTarget.find_first_of(strTok)) != strTarget.npos)
    {
        if (nCutPos > 0)
        {
            strResult[nIndex++] = strTarget.substr(0, nCutPos);
        }
        strTarget = strTarget.substr(nCutPos+1);
    }
 
    if(strTarget.length() > 0)
    {
        strResult[nIndex++] = strTarget.substr(0, nCutPos);
    }
 
    return strResult;
}


void viCom()
{
    //vibration to ras_sub communication
	while(check)
	{
	
	if (serialDataAvail(fd1)) {
		char newChar = serialGetchar(fd1);
		string temp = charstoString(newChar); // data from sensors
		if (temp != "")
		{	
			imflag++;
			cin.clear();
			cout << temp<<endl;
			string *_temp;
			_temp = StringSplit(temp,"\t");
			
			int module = temp[0] - 48;
			double data = stringToDouble(_temp[1]);
			
			//trash data filtering
			if(imflag > 21)
			{
				if(f1 == 0)
					f1 = module;
				if(imdata[module-1] < data)
				{
					imdata[module-1] = data;
				}
			}
			flag = 1;
		}
	}
	}
}

void bluetoothCom()
{
    //bluetooth read data part
	while(check)
	{
		char *temp = read_server();
		string *phone = StringSplit(temp,"#");

		int callsign = temp[0] - 48;
		
		if(callsign == 1)
		{
			senddata(callsign,phone[1],"");
		}
		else if(callsign == 2)
		{
			senddata(callsign,phone[1],phone[2]);
		}
	}
}

void senddata(int type, string number, string message)
{
    //send data to raspberry main
    int   client_socket;
    struct sockaddr_in   server_addr;
	
	client_socket  = socket( PF_INET, SOCK_STREAM, 0);
	if( -1 == client_socket)
	{
		printf( "error\n");
		exit( 1);
	}
	memset( &server_addr, 0, sizeof( server_addr));
	server_addr.sin_family     = AF_INET;
	server_addr.sin_port       = htons( 4000);
	server_addr.sin_addr.s_addr= inet_addr("192.168.2.2");

	if( -1 == connect( client_socket, (struct sockaddr*)&server_addr, sizeof( server_addr) ) )
	{
		printf( "error\n");
		exit(1);
	}
	
	//type 1 = calling state
	if (type == 1)
	{
		disstate = 2;
		string send;
		send = "g2";
		send += "/";
		send += number;
		write( client_socket, send.c_str(), send.size()+1);
	}
	
	//type 2 = send sms state
	else if(type == 2)
	{
		disstate = 3;
		string send;
		send = "g3";
		send += "/";
		send += number;
		send += "/";
		send += message;
		write( client_socket, send.c_str(), send.size()+1);
	}
	
	//type 3 = auto reply
	else if(type == 3)
	{
		disstate = 1;
		string send;
		send ="g4";
		write( client_socket, send.c_str(), send.size()+1);
	}
	//type 4 = ignore state
	else if(type == 4)
	{
		disstate = 1;
		string send;
		send = "g1";
		write( client_socket, send.c_str(), send.size()+1);
	}
	//type 5 = emergency state
	else if(type == 5)
	{
		string send;
		send = "g5";
		write( client_socket, send.c_str(), send.size()+1);
	}
	
	close( client_socket);
}


void gestureCom()
{
    // gesture to ras_sub communication
	while(check)
	{
		if (serialDataAvail(fd2))
		{
			char newChar = serialGetchar(fd2);
			string temp2 = charstoString(newChar);

			if (temp2 != "")
			{	
				cin.clear();
				gesturesig = temp2[0] - 48;
				if(disstate == 2 || disstate == 3)
				{
					if(gesturesig == 3)
					{
						write_server("s1\n");
						senddata(3,"","");
					}
					else if (gesturesig == 1 || gesturesig == 2)
					{
						senddata(4,"","");
					}
				}
			}
		}
	}
}

void accCom()
{
    // input acceleration data
	const float  AccFactor=16.0 /32768.0;
    float Gtotal = 0;
    float GSumSquare = 0;
	float Gdetec = 0;

	int fd;
    char fileName[] = "/dev/i2c-1";
    int  address = 0x68;
	
    if ((fd = open(fileName, O_RDWR)) < 0) {
        printf("Failed to open i2c port\n");
        exit(1);
    }
	
    if (ioctl(fd, I2C_SLAVE, address) < 0) {
        printf("Unable to get bus access to talk to slave\n");
        exit(1);
    }
    
    int8_t power = i2c_smbus_read_byte_data(fd, MPU_POWER1);
    i2c_smbus_write_byte_data(fd, MPU_POWER1, ~(1 << 6) & power);

    while (check) {
        int16_t temp = i2c_smbus_read_byte_data(fd, MPU_TEMP1) << 8 |
                        i2c_smbus_read_byte_data(fd, MPU_TEMP2);

        int16_t xaccel = i2c_smbus_read_byte_data(fd, MPU_ACCEL_XOUT1) << 8 |
                         i2c_smbus_read_byte_data(fd, MPU_ACCEL_XOUT2);
        int16_t yaccel = i2c_smbus_read_byte_data(fd, MPU_ACCEL_YOUT1) << 8 |
                         i2c_smbus_read_byte_data(fd, MPU_ACCEL_YOUT2);
        int16_t zaccel = i2c_smbus_read_byte_data(fd, MPU_ACCEL_ZOUT1) << 8 |
                         i2c_smbus_read_byte_data(fd, MPU_ACCEL_ZOUT2);

		GSumSquare = ((float)xaccel) * xaccel;
        GSumSquare += ((float)yaccel) * yaccel;
        GSumSquare += ((float)zaccel) * zaccel;

        Gtotal = sqrt(GSumSquare);
		Gdetec = AccFactor * Gtotal;
		
		if(Gdetec >= 18 || Gdetec <= -18)
		{
		    //3sec term
			acc = 1;
			sleep(3);
			acc = 0;
		}
		    
	}
	
}

void scope()
{
	//trouble type decide
	int m1 = 0;
	int m2 = 0;
	int m3 = 0;
	int j = 0;

	double tmp1 = 0;
	double tmp2 = 0;
	double tmp3 = 0;

	while(check)
	{
		//check process
		if(flag == 1 && imflag > 21)
		{
			
			sleep(3);
			
			for(j=0; j<8 ;j++)
			{
				if(tmp1 < imdata[j])
				{
					tmp1 = imdata[j];
					m1 = j;
				}
			}

			for(j=0; j<8 ;j++)
			{
				if(j == m1)
				{
					continue;
				}
				if(tmp2 < imdata[j])
				{
					tmp2 = imdata[j];
					m2 = j;
				}

			}
			
			for(j=0; j<8 ;j++)
			{
				if(j == m1 || j == m2)
				{
					continue;
				}
				if(tmp3 < imdata[j])
				{
					tmp3 = imdata[j];
					m3 = j;
				}
			}
			
			if(f1 == 8)
			{
				write_server("e2\n");
			}
			else if(imdata[m1] >= 800)
			{
				write_server("e1\n");
			}
			else if (imdata[m1] >= 700 && acc == 1)
			{
				write_server("e1\n");
			}
			else if(f1 == 1 || f1 == 3 || f1 ==5 || f1 == 7)
			{
				write_server("e2\n");
			}
			else if(f1 == 8 || f1 == 4)
			{
				write_server("e3\n");
			}
			else if(m1 != 0 && m2 != 0)
			{
				double imtemp = (imdata[m1]+imdata[m2]) / 2;
				if(imtemp >= 700)
				{
					write_server("e1\n");
				}
			}
			else if(m2 != 0 && m3 != 0)
			{
				double imtemp2 = (imdata[m1]+imdata[m2]+imdata[m3]) / 3;
				if(imtemp2 >= 600)
				{
					write_server("e1\n");
				}
			}
			else
			{
				write_server("e2\n");
			}
			senddata(5,"","");
			sleep(3);
			// process stop
			exit(1);
			check = 0;
			flag = 0;
		}

		
			
	}

}
