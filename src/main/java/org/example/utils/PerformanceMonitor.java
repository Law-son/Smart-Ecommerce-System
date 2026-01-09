package org.example.utils;

/**
 * Utility class for performance monitoring.
 * Follows Single Responsibility Principle - only handles performance timing.
 */
public class PerformanceMonitor {
    
    /**
     * Executes an operation and logs its execution time.
     *
     * @param operationName Name of the operation being monitored
     * @param operation The operation to execute
     * @param <T> Return type of the operation
     * @return Result of the operation
     */
    public <T> T monitor(String operationName, Operation<T> operation) {
        long startTime = System.currentTimeMillis();
        T result = operation.execute();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("[PERF] " + operationName + " executed in " + duration + " ms");
        return result;
    }
    
    /**
     * Functional interface for operations to be monitored.
     *
     * @param <T> Return type of the operation
     */
    @FunctionalInterface
    public interface Operation<T> {
        T execute();
    }
}



