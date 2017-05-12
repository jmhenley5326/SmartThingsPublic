/**
* 
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.01
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*
*/

// Automatically generated. Make future change here.

definition(
name: "Thermostat Fan Director",
namespace: "smartthings",
author: "Tim Slagle Modified by: Jason Henley",
description: "Adjust your thermostat fan based on the temperature of a secondary temperature sensor",
category: "Green Living",
iconUrl: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png",
iconX2Url: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png"
)

section
{
input "thermostat", "capability.thermostat", title: "Which Thermostat?", multi: false, required: true
input "tempSensor", "capability.temperatureMeasurement", title: "Which temperature sensor?", multi: false, required: true
input "tempDiff", "number", title: "Temperature Difference to turn fan on?", required: true, defaultValue: 2
input "sendPushMessage", "bool", title: "Send a push notification?", required: false, defaultValue: true
input "sendSMS", "phone", title: "Send as SMS?", required: false, defaultValue: false

}

def installed(){
log.debug "Installed called with ${settings}"
init()
}

def updated(){
log.debug "Updated called with ${settings}"
unsubscribe()
init()
}

def init(){
runIn(60, "temperatureHandler")
subscribe(tempSensor, "temperature", temperatureHandler)
subscribe(thermostat, "temperature", temperatureHandler)
}

def temperatureHandler(evt) {

log.debug "Temperature Handler"
//get the latest temp readings and compare
def currentThermostatTemp = thermostat.latestValue("temperature")
def currentSensorTemp = tempSensor.latestValue("temperature")

def currentThermFan = thermostat.latestValue("thermostatFanMode")

log.debug "Thermostat: ${currentThermostatTemp}"
log.debug "Temp Sensor: ${currentSensorTemp}"
log.debug "Current Fan Mode: ${currentThermFan}"

def difference = (currentThermostatTemp - currentSensorTemp)

//log.debug "Temp Difference: ${difference}"

if(difference < 0){
	difference *= -1
    }
 //log.debug "Temp Difference Now: ${difference}"


if( difference >= tempDiff ){
	if(currentThermFan != "fanOn")
	{
		//set the therm fan state to ON
		def msg = "I changed your ${thermostat} fan mode to ON because temperature is out of range from ${tempSensor}"
		thermostat?.fanOn()
        thermostat?.poll()
		log.debug msg
		sendMessage(msg)
	}
} 
else
{
	if(currentThermFan != "fanAuto")
	{
		//set the therm fan state to AUTO
		def msg = "I changed your ${thermostat} fan mode to AUTO because temperature difference is in range of ${tempSensor}"
		thermostat?.fanAuto()
        thermostat?.poll()
		log.debug msg
		sendMessage(msg)
	}
}
}

private sendMessage(msg){
if (sendPushMessage == true) {
sendPush(msg)
}
if (sendSMS != null) {
sendSms(sendSMS, msg) 
}

}