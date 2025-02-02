package com.riders.thelab.core.data.remote.dto.flight

import kotlinx.serialization.SerialName
import kotools.types.text.NotBlankString
import java.io.Serializable

@kotlinx.serialization.Serializable
data class Arrivals(
    /* Either the operator code followed by the flight number for the flight (for commercial flights)
    or the aircraft's registration (for general aviation). */
    @SerialName("ident")
    val operatorID: NotBlankString,

    @SerialName("ident_icao")
    val identICAO: NotBlankString? = null,

    @SerialName("ident_iata")
    val identIATA: NotBlankString? = null,
    /* Actual departure runway at origin, when known */
    @SerialName("actual_runway_off")
    val actualRunwayOff: NotBlankString? = null,
    /* Actual arrival runway at destination, when known */
    @SerialName("actual_runway_on")
    val actualRunwayOn: NotBlankString? = null,

    @SerialName("fa_flight_id")
    val faFlightID: NotBlankString,

    @SerialName("operator")
    val operator: NotBlankString? = null,

    @SerialName("operator_icao")
    val operatorICAO: NotBlankString? = null,

    @SerialName("operator_iata")
    val operatorIATA: NotBlankString? = null,

    @SerialName("flight_number")
    val flightNumber: NotBlankString? = null,

    @SerialName("registration")
    val registration: NotBlankString? = null,

    /* The ident of the flight for Air Traffic Control purposes, when known and different from ident.*/
    @SerialName("atc_ident")
    val atcIdent: NotBlankString? = null,

    @SerialName("inbound_fa_flight_id")
    val inboundFaFlightID: NotBlankString? = null,

    @SerialName("codeshares")
    val codeShares: List<NotBlankString>,

    @SerialName("codeshares_iata")
    val codeSharesIata: List<NotBlankString>,

    /* Flag indicating whether this flight is blocked from public viewing.*/
    @SerialName("blocked")
    val blocked: Boolean,
    @SerialName("diverted")
    val diverted: Boolean,
    @SerialName("cancelled")
    val cancelled: Boolean,
    @SerialName("position_only")
    val positionOnly: Boolean,
    @SerialName("origin")
    val origin: OriginDestination,
    @SerialName("destination")
    val destination: OriginDestination? = null,
    /*
     * Departure delay (in seconds) based on either actual or estimated gate departure time.
     * If gate time is unavailable then based on runway departure time.
     * A negative value indicates the flight is early.
     */
    @SerialName("departure_delay")
    val departureDelay: Int? = null,
    /*
     * Arrival delay (in seconds) based on either actual or estimated gate arrival time.
     * If gate time is unavailable then based on runway arrival time.
     * A negative value indicates the flight is early.
     */
    @SerialName("arrival_delay")
    val arrivalDelay: Int? = null,
    /* Runway-to-runway filed duration (seconds). */
    @SerialName("filed_ete")
    val filedEte: Int? = null,
    /*
     * The percent completion of a flight, based on runway departure/arrival.
     * Null for en route position-only flights.
     * Constraints: Min 0┃Max 100
     */
    @SerialName("progress_percent")
    val progress: Int?,
    @SerialName("status")
    val status: NotBlankString,
    @SerialName("aircraft_type")
    val aircraftType: NotBlankString? = null,
    @SerialName("route_distance")
    val routeDistance: Int?,
    /* Filed IFR airspeed (knots). */
    @SerialName("filed_airspeed")
    val filedAirSpeed: Int? = null,
    /* Filed IFR altitude (100s of feet). */
    @SerialName("filed_altitude")
    val filedAltitude: Int? = null,
    /* The textual description of the flight's route. */
    @SerialName("route")
    val route: NotBlankString? = null,
    @SerialName("baggage_claim")
    val baggageClaim: NotBlankString? = null,
    @SerialName("seats_cabin_business")
    val seatsCabinBusiness: Int? = null,
    @SerialName("seats_cabin_coach")
    val seatsCabinCoach: Int? = null,
    @SerialName("seats_cabin_first")
    val seatsCabinFirst: Int? = null,
    @SerialName("gate_origin")
    val gateOrigin: NotBlankString? = null,
    @SerialName("gate_destination")
    val gateDestination: NotBlankString? = null,
    @SerialName("terminal_origin")
    val terminalOrigin: NotBlankString? = null,
    @SerialName("terminal_destination")
    val terminalDestination: NotBlankString? = null,
    @SerialName("type")
//    val type: FlightType,
    val type: String,
    /* Scheduled gate departure time. */
    @SerialName("scheduled_out")
    val scheduledOut: NotBlankString? = null,
    /* Estimated gate departure time. */
    @SerialName("estimated_out")
    val estimatedOut: NotBlankString? = null,
    /* Actual gate departure time. */
    @SerialName("actual_out")
    val actualOut: NotBlankString? = null,
    /* Scheduled runway departure time. */
    @SerialName("scheduled_off")
    val scheduledOff: NotBlankString? = null,
    /* Estimated runway departure time. */
    @SerialName("estimated_off")
    val estimatedOff: NotBlankString? = null,
    /* Actual runway departure time. */
    @SerialName("actual_off")
    val actualOff: NotBlankString? = null,
    /* Scheduled runway arrival time. */
    @SerialName("scheduled_on")
    val scheduledOn: NotBlankString? = null,
    /* Estimated runway arrival time. */
    @SerialName("estimated_on")
    val estimatedOn: NotBlankString? = null,
    /* Actual runway arrival time. */
    @SerialName("actual_on")
    val actualOn: NotBlankString? = null,
    /*Scheduled gate arrival time. */
    @SerialName("scheduled_in")
    val scheduledIn: NotBlankString? = null,
    /* Estimated gate arrival time. */
    @SerialName("estimated_in")
    val estimatedIn: NotBlankString? = null,
    /* Actual gate arrival time. */
    @SerialName("actual_in")
    val actualIn: NotBlankString? = null
) : Serializable