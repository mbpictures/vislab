package de.hska.iwi.vislab.lab2.example;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("fibonacci")
public class Fibonacci {

    public static List<Integer> fibonacciCounter = new ArrayList<>();

    public int calcfibonacci(int n) {
        if (n < 2)
            return n;
        return calcfibonacci(n - 1) + calcfibonacci(n - 2);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public String getAllFibonacci() {
        return fibonacciCounter.stream().map(this::calcfibonacci).collect(Collectors.toList()).toString();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{id}")
    public Integer getFibonacci(@PathParam("id") int id) {
        return calcfibonacci(fibonacciCounter.get(id));
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Integer addFibonacci() {
        fibonacciCounter.add(1);
        return fibonacciCounter.size() - 1;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{id}")
    public String updateCounter(@PathParam("id") int id, FibonacciValue value){
        fibonacciCounter.set(id, value.value);
        return Integer.toString(calcfibonacci(fibonacciCounter.get(id)));
    }

    @DELETE
    @Path("/{id}")
    public void deleteFibonacci(@PathParam("id") int id) {
        fibonacciCounter.remove(id);
    }
    
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{id}/increase")
    public String increaseFibonacci(@PathParam("id") int id) {
        fibonacciCounter.set(id, fibonacciCounter.get(id) + 1);
        return Integer.toString(calcfibonacci(fibonacciCounter.get(id)));
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{id}/reset")
    public String resetFibonacci(@PathParam("id") int id) {
        fibonacciCounter.set(id, 1);
        return Integer.toString(calcfibonacci(fibonacciCounter.get(id)));
    }

    public static class FibonacciValue implements Serializable {
        int value;

        public FibonacciValue(){ }

        public FibonacciValue(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
