package tcc;

import tcc.flight.FlightReservationDoc;
import tcc.hotel.HotelReservationDoc;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.GregorianCalendar;

public class TransactionalTestClient {
    private static final int MAX_RETRY = 10;

    public static void main(String[] args) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(TestServer.BASE_URI);

            GregorianCalendar tomorrow = new GregorianCalendar();
            tomorrow.setTime(new Date());

            // try book flight
            int counter = 0;
            int responseFlightStatus = 0;
            Response responseFlight = null;

            while (tryDoing(counter, responseFlightStatus)) {
                responseFlight = tryBookFlight(target, tomorrow);
                responseFlightStatus = responseFlight.getStatus();
                counter++;
                System.out.println("Try to book Flight (" + counter + "/" + MAX_RETRY + ")");
            }

            // try book hotel
            counter = 0;
            int responseHotelStatus = 0;
            Response responseHotel = null;

            while (tryDoing(counter, responseHotelStatus)) {
                responseHotel = tryBookHotel(target, tomorrow);
                responseHotelStatus = responseHotel.getStatus();
                counter++;
                System.out.println("Try to book Hotel (" + counter + "/" + MAX_RETRY + ")");
            }

            // handle the status codes

            FlightReservationDoc outputFlight = responseFlight.readEntity(FlightReservationDoc.class);
            String flightId = extractId(outputFlight.getUrl());

            HotelReservationDoc outputHotel = responseHotel.readEntity(HotelReservationDoc.class);
            String reservationId = extractId(outputHotel.getUrl());

            boolean confirmTransaction = responseFlightStatus == 200 && responseHotelStatus == 200;

            Invocation.Builder flightBuilder = target.path("flight/{id}").resolveTemplate("id", flightId).request()
                    .accept(MediaType.TEXT_PLAIN);
            Invocation.Builder hotelBuilder = target.path("hotel/{id}").resolveTemplate("id", reservationId).request();

            if (confirmTransaction) {
                commit(flightBuilder, hotelBuilder, flightId, reservationId);
            } else {
                rollback(flightBuilder, hotelBuilder, flightId, reservationId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean tryDoing(int counter, int responseFlightStatus) {
        return responseFlightStatus != 200 && responseFlightStatus != 409 && counter < MAX_RETRY;
    }

    private static void commit(Invocation.Builder flightBuilder, Invocation.Builder hotelBuilder, String flightId, String hotelId) {
        System.out.println("Requests succesfully fullfilled. Committing transaction...");

        System.out.println("Confirm flight...");

        int counter = 0;
        int responseFlightStatus = 0;
        Response responseFlight = null;

        while (tryDoing(counter, responseFlightStatus)) {
            responseFlight = flightBuilder.put(Entity.text(""));
            responseFlightStatus = responseFlight.getStatus();
            counter++;
            System.out.println("Try to commit Flight booking (" + counter + "/" + MAX_RETRY + ")");
            System.out.println(responseFlight.readEntity(String.class));
        }

        if (responseFlightStatus != 200) {
            rollback(flightBuilder, hotelBuilder, flightId, hotelId);
            return;
        }

        System.out.println("Confirm hotel...");

        counter = 0;
        int responseHotelStatus = 0;
        Response responseHotel = null;

        while (tryDoing(counter, responseHotelStatus)) {
            responseHotel = hotelBuilder.put(Entity.text(""));
            responseHotelStatus = responseHotel.getStatus();
            counter++;
            System.out.println("Try to commit Hotel booking (" + counter + "/" + MAX_RETRY + ")");
            System.out.println(responseHotel.readEntity(String.class));
        }

        if (responseHotelStatus != 200) {
            rollback(flightBuilder, hotelBuilder, flightId, hotelId);
            return;
        }
        System.out.println("Transaction committed.");
    }

    private static void rollback(Invocation.Builder flightBuilder, Invocation.Builder hotelBuilder, String flightId, String reservationID) {
        System.out.println("Transaction failed. Start rollback.");

        if (flightId != null && !flightId.equals("null")) {
            System.out.println("Cancel flight...");
            Response delResponse = flightBuilder.delete();
            System.out.println(delResponse.readEntity(String.class));
        }

        if (reservationID != null && !reservationID.equals("null")) {
            System.out.println("Cancel hotel..." + reservationID);
            Response delResponseHotel = hotelBuilder.delete();
            System.out.println(delResponseHotel.readEntity(String.class));
        }

        System.out.println("Transaction rollbacked.");
    }

    private static Response tryBookHotel(WebTarget target, GregorianCalendar tomorrow) {
        WebTarget webTargetHotel = target.path("hotel");

        HotelReservationDoc docHotel = new HotelReservationDoc();
        docHotel.setName("Christian");
        docHotel.setHotel("Interconti");
        docHotel.setDate(tomorrow.getTimeInMillis());

        Response responseHotel = webTargetHotel.request().accept(MediaType.APPLICATION_XML)
                .post(Entity.xml(docHotel));
        return responseHotel;
    }

    private static Response tryBookFlight(WebTarget target, GregorianCalendar tomorrow) {
        WebTarget webTargetFlight = target.path("flight");

        FlightReservationDoc docFlight = new FlightReservationDoc();
        docFlight.setName("Christian");
        docFlight.setFrom("Karlsruhe");
        docFlight.setTo("Berlin");
        docFlight.setAirline("airberlin");
        docFlight.setDate(tomorrow.getTimeInMillis());

        Response responseFlight = webTargetFlight.request().accept(MediaType.APPLICATION_XML)
                .post(Entity.xml(docFlight));
        return responseFlight;
    }

    private static String extractId(String url) {
        if (url == null) {
            return "null";
        }
        String[] parts = url.split("/");
        String id = parts[parts.length - 1];
        return id;
    }
}
