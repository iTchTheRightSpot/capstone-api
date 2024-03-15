package dev.capstone.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }

    public OutOfStockException() {
        super("resource out of stock");
    }
}
