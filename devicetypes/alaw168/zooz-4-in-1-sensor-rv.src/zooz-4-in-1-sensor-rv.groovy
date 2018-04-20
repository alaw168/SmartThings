/*
 * V 1.2 of zooZ 4-in-1 sensor code 6/12/2016
 * Adapted from V1.2 of device handler originally by Robert Vandervoort
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * 
 */
 
 metadata {
	definition (name: "zooZ 4-in-1 Sensor", namespace: "alaw168", author: "Alvin Law") {
		capability "Motion Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
		
        // RAW Description: 0 0 0x0701 0 0 0 e 0x5E 0x98 0x86 0x72 0x5A 0x85 0x59 0x73 0x80 0x71 0x31 0x70 0x84 0x7A   										 
		attribute "tamper", "enum", ["active", "inactive"]
		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x31,0x70,0x5A,0x98,0x7A"
		}
simulator {
		status "No Motion" : "command: 9881, payload: 00300300"
		status "Motion"    : "command: 9881, payload: 003003FF"
        status "Clear" : " command: 9881, payload: 0071050000000007030000"
        status "Tamper" : "command: 9881, payload: 007105000000FF07030000"
        
        for (int i = 0; i <= 100; i += 20) {
			status "Temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                	scaledSensorValue: i,
                    precision: 1,
                    sensorType: 1,
                    scale: 1
				)
			).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 20) {
			status "Relative Humidity ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                	scaledSensorValue: i,
                    sensorType: 5
            	)
			).incomingMessage()
		}
		for (int i in [0, 1, 2, 8, 12, 16, 20, 24, 30, 64, 82, 100]) {
			status "Luminance ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i,
                sensorType: 3
                )
			).incomingMessage()
		}
		for (int i in [0, 5, 10, 15, 50, 99, 100]) {
			status "Battery ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
                batteryLevel: i
                )
			).incomingMessage()
		}
		status "Low Battery Alert": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
            	batteryLevel: 255
            	)
			).incomingMessage()
		status "wake up": "command: 8407, payload:"
	}
	tiles (scale: 2) {
		multiAttributeTile(name:"main", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc"
			}
		}
		valueTile("temperature","device.temperature", width: 2, height: 2) {
            	state "temperature", label:'${currentValue}°', unit:"${unit}", /*icon:"st.Weather.weather2",*/ backgroundColors:[
                	[value: 32, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
			}
		valueTile("humidity","device.humidity", width: 2, height: 2) {
           	state "humidity", label:'${currentValue}%', icon:"st.Weather.weather12"/*, backgroundColors:[
                	[value: 30, color: "#bc2323"],
                    [value: 40, color: "#f1d801"],
					[value: 50, color: "#ffffff"],
					[value: 60, color: "#ffffff"],
					[value: 70, color: "#ffffff"],
					[value: 80, color: "#1e9cbb"],
					[value: 100, color: "#153591"]
				]*/
			}
		valueTile("illuminance","device.illuminance", width: 2, height: 2) {
            state "luminosity", label:'${currentValue}%', icon:"st.illuminance.illuminance.light"/*, backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 60, color: "#ffffff"]
				]*/
			}
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state("tamper", label:'tampered', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
			state("clear", label:'ok', icon:"st.contact.contact.closed", backgroundColor:"#cccccc")
			state("active", label:'tampered', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
			state("inactive", label:'ok', icon:"st.contact.contact.closed", backgroundColor:"#cccccc")
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
	        state "default", action: "refresh", label: "", icon: "st.secondary.refresh"
		}
		main(["main"])
		details(["main","temperature","humidity","illuminance","tampering","battery","refresh"])
	}
    preferences {
		input "debugOutput", "boolean", 
			title: "Enable debug logging?",
			defaultValue: false,
			displayDuringSetup: true
		input "LEDbehavior", "enum",
			title: "LED Behavior",
			options: ["Off", "Temp Pulse / Motion Flash", "Temp / Motion Flash", "Motion Flash"],
            defaultValue: "Motion Flash",
			required: false,
			displayDuringSetup: false
		input "TempSensitivity", "number",
			title: "Temperature Reporting Sensitivity",
            description: "Set temperature change to be reported by the sensor [1-50] (0.1 - 5 degrees)",
            range: "1..50",
			defaultValue: 10,
            required: false,
            displayDuringSetup: false
		input "HumiditySensitivity", "number",
            title: "Humidity Reporting Sensitivity",
            description: "Set humidity percentage change to be reported by the sensor [1-50]%",
			range: "1..50",
			defaultValue: 10,
			required: false,
            displayDuringSetup: false
		input "LuminanceSensitivity", "number",
            title: "Luminance Reporting Sensitivity",
            description: "Set light percentage change to be reported by the sensor [5-50]%",
            range: "5..50",
			defaultValue: 10,
            required: false,
	        displayDuringSetup: false
		input "PIRsensitivity", "number",
    	    title: "PIR motion sensitivity",
			description: "A value from 1-7, from very high (1) to very low (7) sensitivity",
			range: "1..7",
			defaultValue: 3,
			required: false,
			displayDuringSetup: true
		input "MotionReset", "number",
    	    title: "PIR trigger reset interval",
			description: "Set the time when motion is reported again after initial trigger [15-60 sec]",
			range: "15..60",
			defaultValue: 15,
			required: false,
			displayDuringSetup: true            
	}
}

def updated()
{
	updateDataValue("configured", "false")
	state.debug = ("true" == debugOutput)
}

def parse(String description)
{
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x31: 5, 0x71:3, 0x7A: 2, 0x81: 1, 0x84: 2, 0x86: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (state.debug) log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) 
{
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	if (!isConfigured()) {
		// we're still in the process of configuring a newly joined device
		if (state.debug) log.debug("late configure")
		result += response(configure())
	} else {
		result += response(checkBattery())
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x71: 3, 0x7A: 2, 0x81: 1, 0x84: 2])
	state.sec = 1
	// if (state.debug) log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	if (state.debug) log.debug "---SECURITY COMMANDS SUPPORTED REPORT V1--- ${device.displayName} sent commandClassControl: ${cmd.commandClassControl}, commandClassSupport: ${cmd.commandClassSupport}, reportsToFollow: ${cmd.reportsToFollow}"
	response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
	if (state.debug) log.debug "---MANUFACTURER SPECIFIC REPORT V2--- ${device.displayName} sent deviceIdDataFormat: ${cmd.deviceIdDataFormat}, deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}, deviceIdType: ${cmd.deviceIdType}, payload: ${cmd.payload}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) {
	if (state.debug) log.debug "---COMMAND CLASS VERSION REPORT V1--- ${device.displayName} has command class version: ${cmd.commandClassVersion} - payload: ${cmd.payload}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	if (state.debug) log.debug "---VERSION REPORT V1--- ${device.displayName} is running firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if (state.debug) log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    if (state.debug) log.debug "---CONFIGURATION REPORT V2--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
		createEvent(map)
	} else {
		map.value = cmd.batteryLevel
		createEvent(map)
	}
	state.lastbatt = now()
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	if (state.debug) log.debug "---SENSOR MULTILEVEL v5 REPORT--- ${device.displayName} sent sensorType: ${cmd.sensorType} value: ${cmd.sensorValue} scale: ${cmd.scale} scaledSensorValue: ${cmd.scaledSensorValue}"
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, 0/*cmd.precision*/)
			map.unit = getTemperatureScale()
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break;
        case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break;
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def motionEvent(value) {
	def map = [name: "motion"]
	if (value != 0) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
		map.value = "inactive"
		map.descriptionText = "$device.displayName motion has stopped"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	if (state.debug) log.debug "---SENSOR BINARY REPORT V2--- ${device.displayName} sent value: ${cmd.sensorValue}"
	motionEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	if (state.debug) log.debug "---BASIC SET REPORT V1--- ${device.displayName} sent value: ${cmd.value}"
	motionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
if (state.debug) log.debug "---NOTIFICATION REPORT V3--- ${device.displayName} sent eventParametersLength: ${cmd.eventParametersLength} eventParameter: ${cmd.eventParameter} notificationType: ${cmd.notificationType} event: ${cmd.event}"	
def result = []
if (cmd.notificationType == 7) {
	if (cmd.event == 0x00 && cmd.eventParameter == 0x08) {
		result << motionEvent(0)
        }
    else if (cmd.event == 0x03) {
    	result << createEvent(name: "tamper", value: "active", descriptionText: "$device.displayName cover is open.")
		}
	else if (cmd.event == 0X00 || cmd.eventParameter == 0x03) {
		result << createEvent(name: "tamper", value: "inactive", descriptionText: "$device.displayName cover is closed.")
    }
	else if (cmd.event == 0x08) {
    	result << motionEvent(255)
	}
}
else {
	result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

private checkBattery() {
	def request = [
		zwave.batteryV1.batteryGet(),
		zwave.wakeUpV1.wakeUpNoMoreInformation()
		]
	commands(request)
}

def configure() {
	// This sensor joins as a secure device if you double-click the button to include it
	//if (device.device.rawDescription =~ /98/ && !state.sec) {
	//	if (state.debug) log.debug "4-in-1 sensor not sending configure until secure"
	//	return []
	//}
	if (state.debug) log.debug "--Sending configuration commands to zooZ 4-in-1 sensor--"
    //if (state.debug) log.debug "Prefernces settings: PIRsensitivity: $PIRsensitivity, Temp Sensitivity: $TempSensitivity, Humidity Sensitivity: $HumiditySensitivity, Luminance Sensitivity: $LuminanceSensitivity"
	def LEDbehav = 3 /* "Off", "Temp Pulse / Motion Flash", "Temp / Motion Flash", "Motion Flash" */
	if (LEDbehavior == "Off") {
    	LEDbehav = 1
    }
	else if (LEDbehavior == "Temp Pulse / Motion Flash") {
    	LEDbehav = 2
	}
	else if (LEDbehavior == "Motion Flash") {
    	LEDbehav = 4
	}
	else {
		LEDbehav = 3
	}	
	def PIRsens = 3
	if (PIRsensitivity) {
		PIRsens=PIRsensitivity
	}
	else {
		PIRsens = 3
	}
    def MotionRst = 15
	if (MotionReset) {
		MotionRst=MotionReset
	}
	else {
		MotionRst = 3
	}
	def tempoff = 1
	if (TempSensitivity) {
		tempoff = TempSensitivity
	}
	else {
		tempoff = 1
	}
	def humidityoff = 10
	if (HumiditySensitivity) {
		humidityoff = HumiditySensitivity
	}
	else {
		humidityoff = 10
	}
	def luminanceoff = 10
	if (LuminanceSensitivity) {
		luminanceoff = LuminanceSensitivity
	}
	else {
		luminanceoff = 10
	}
    if (state.debug) log.debug "settings: ${settings.inspect()}, state: ${state.inspect()}"
    setConfigured()
	def request = [
		// set wakeup interval to 20 mins
		zwave.wakeUpV1.wakeUpIntervalSet(seconds:3600, nodeid:zwaveHubNodeId),
		
		// Get Version information
        zwave.versionV1.versionGet(),
        zwave.firmwareUpdateMdV2.firmwareMdGet(),
		
        // configure temp scale to celcius or fahrenheight and set offset
		zwave.configurationV1.configurationSet(parameterNumber: 0x01, size: 1, scaledConfigurationValue: 1),
		zwave.configurationV1.configurationGet(parameterNumber: 0x01),
		zwave.configurationV1.configurationSet(parameterNumber: 0x02, size: 1, scaledConfigurationValue: tempoff),		
		zwave.configurationV1.configurationGet(parameterNumber: 0x02),
		
        // configure humidity offset
		zwave.configurationV1.configurationSet(parameterNumber: 0x03, size: 1, scaledConfigurationValue: humidityoff),
		zwave.configurationV1.configurationGet(parameterNumber: 0x03),
		
        // configure luminance offset
		zwave.configurationV1.configurationSet(parameterNumber: 0x04, size: 1, scaledConfigurationValue: luminanceoff),
		zwave.configurationV1.configurationGet(parameterNumber: 0x04),
		
		// send no-motion report x minutes after motion stops
		zwave.configurationV1.configurationSet(parameterNumber: 0x05, size: 1, scaledConfigurationValue: MotionRst),
		zwave.configurationV1.configurationGet(parameterNumber: 0x05),
    	
		// set motion sensor sensitivity
        zwave.configurationV1.configurationSet(parameterNumber: 0x06, size: 1, scaledConfigurationValue: PIRsens),
		zwave.configurationV1.configurationGet(parameterNumber: 0x06),
		
        // set LED behavior
        zwave.configurationV1.configurationSet(parameterNumber: 0x07, size: 1, scaledConfigurationValue: LEDbehav),
        zwave.configurationV1.configurationGet(parameterNumber: 0x07),
		
		// get updated battery and sensor data
        zwave.batteryV1.batteryGet(),
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1),
        zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:0),
        zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:0),
        
		// Can use the zwaveHubNodeId variable to add the hub to the device's associations:
		zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
    ]
	commands(request) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
}

private setConfigured() {
	updateDataValue("configured", "true")
    return []
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1000) {
	delayBetween(commands.collect{ command(it) }, delay)
}

def refresh() {	
	if (device.currentValue("tamper") != "inactive") {
		logDebug "Clearing Tamper"
		sendEvent(createTamperEventMap("inactive"))
	}
	else if (state.pendingRefresh) {	
		sendEvent(createEventMap("pendingChanges", configParams.size(), "", false))			
		state.refreshAll = true		
		logForceWakeupMessage "All configuration settings will be sent to the device and its data will be refreshed the next time it wakes up."
	}
	else {
		state.pendingRefresh = true
		logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	}
	return []
}

