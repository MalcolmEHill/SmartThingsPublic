/**
 *  Delay before turning things on
 *
 *  Author: Docwisdom
 *  Based off of code by SmartThings
 *  Date: 2014-08-29
 */
definition(
    name: "Delay Switch",
    namespace: "docwisdom",
    author: "Brian Critchlow",
    description: "Turns on an outlet when the user is present after a delay and off after a period of time",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("When someone's around because of...") {
		input name: "motionSensors", title: "Motion here", type: "capability.motionSensor", multiple: true, required: false
		input name: "presenceSensors", title: "And (optionally) these sensors being present", type: "capability.presenceSensor", multiple: true, required: false
	}
    section("And this time has elapsed") {
		input name: "delayseconds", title: "Seconds?", type: "number", multiple: false
	}
	section("Turn on these outlet(s)") {
		input name: "outlets", title: "Which?", type: "capability.switch", multiple: true
	}
	section("For this amount of time") {
		input name: "minutes", title: "Minutes?", type: "number", multiple: false
	}
}

def installed() {
	subscribeToEvents()
}

def updated() {
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(motionSensors, "motion.active", motionActive)
	subscribe(motionSensors, "motion.inactive", motionInactive)
	subscribe(presenceSensors, "presence.not present", notPresent)
    subscribe(presenceSensors, "presence.present", Present)
}

def motionActive(evt) {
	log.debug "$evt.name: $evt.value"
		outletsOn()
}

def motionInactive(evt) {
	log.debug "$evt.name: $evt.value"
	if (allQuiet()) {
		outletsOff()
	}
}

def notPresent(evt) {
	log.debug "$evt.name: $evt.value"
	if (!anyHere()) {
    	log.debug "Bye bye now"
		outletsOff()
	}
}
def Present(evt) {
	log.debug "$evt.name: $evt.value"
	if (anyHere()) {
    	log.debug "Welcome back"
		outletsOn()
	}
}

def allQuiet() {
	def result = true
	for (it in motionSensors) {
		if (it.currentMotion == "active") {
			result = false
			break
		}
	}
	return result
}

def anyHere() {
	def result = false
	for (it in presenceSensors) {
		if (it.currentPresence == "present") { 
			result = true
			break
		}
	}
	return result
}

def outletsOn() {
	log.debug "Lights on in ${delayseconds} seconds"
    def weirdDelay = delayseconds * 1 //work-around for odd scheduling behavior
    unschedule("scheduledTurnOff")
	runIn(weirdDelay, "scheduledTurnOn")
}

def outletsOff() {
	log.debug "Lights off in ${minutes} minutes"
	def delay = minutes * 60
    unschedule("scheduledTurnOn")
	runIn(delay, "scheduledTurnOff")
}

def scheduledTurnOn() {
	outlets.on()
	unschedule("scheduledTurnOn") // Temporary work-around to scheduling bug
}

def scheduledTurnOff() {
	outlets.off()
	unschedule("scheduledTurnOff") // Temporary work-around to scheduling bug
}