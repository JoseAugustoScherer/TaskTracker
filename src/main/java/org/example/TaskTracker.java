package main.java.org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class TaskTracker {

    private static final String FILE = "tasks.json";
    private static Scanner scanner = new Scanner ( System.in );
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        TaskTracker app = new TaskTracker();

        app.initializeFile();

        boolean proceed = true;
        while ( proceed ) {
            app.showMenu();
            int option = scanner.nextInt();

            switch ( option ) {
                case 1:
                    app.crateTask();
                    break;
                case 2:
                    app.editTask();
                    break;
                case 3:
                    app.deleteTask();
                    break;
                case 4:
                    app.listAllTasks();
                    break;
                case 5:
                    app.listAllTasksDone();
                    break;
                case 6:
                    app.listAllTasksNotDone();
                    break;
                case 7:
                    app.listAllTasksInProgress();
                    break;
                case 8:
                    proceed = false;
                    System.out.println( "Exiting..." );
                    break;
                default:
                    System.out.println( "Invalid option! Try again." );
            }
        }
        scanner.close();
    }

    // Menu
    private void showMenu () {
        System.out.println( "\n" + "=".repeat(30) );
        System.out.println( "======   TASK TRACKER   ======" );
        System.out.println( "=".repeat(30) );
        System.out.println( "\n 1. Create a task." );
        System.out.println( "\n 2. Update a task." );
        System.out.println( "\n 3. Delete a task." );
        System.out.println( "\n 4. List all tasks." );
        System.out.println( "\n 5. List all tasks that are done." );
        System.out.println( "\n 6. List all tasks that are not done." );
        System.out.println( "\n 7. List all tasks that are in progress." );
        System.out.println( "\n 8. EXIT." );
        System.out.print( "\n 9. Choose one option: " );
    }

    // ==================
    // == Main methods ==
    // ==================

    // 1. Create task
    private void crateTask () {
        System.out.println( "\n" + "=".repeat(30) );
        System.out.println( "\n\t CREATE NEW TASK: " );
        System.out.println( "\n" + "=".repeat(30) );

        try {
            scanner.nextLine();

            System.out.print( "Task Description: " );
            String description = scanner.nextLine().trim();
            if ( description.isEmpty() ) {
                System.out.println( "Description cannot be empty!" );
                return;
            }

            String taskStatus = "";

            System.out.print( "What is the status of the task? Todo, in-progress or done? " );
            String status = scanner.nextLine().trim().toLowerCase();
            if ( status.isEmpty() ) {
                System.out.println( "Status cannot be empty!" );
                return;
            } else if ( status.equalsIgnoreCase( "todo" ) || status.equalsIgnoreCase( "in-progress" ) || status.equalsIgnoreCase( "done" ) ) {
                taskStatus = status;
            } else {
                System.out.println( "Invalid status!" );
                return;
            }

            String createdAt = LocalDateTime.now().format( formatter );

            JSONObject data = loadData();
            JSONArray tasks = data.getJSONArray( "tasks" );

            int nextID = generateNextID( tasks );

            JSONObject newTask = new JSONObject();
            newTask.put( "id", nextID );
            newTask.put( "description", description );
            newTask.put( "status", taskStatus );
            newTask.put( "createdAt", createdAt );
            newTask.put( "updatedAt", "n/d" );

            tasks.put( newTask );

            saveData( data );

        } catch ( Exception e ) {
            System.err.println( "Error adding task: " + e.getMessage() );
        } finally {
            System.out.println( "\n\t DONE!" );
        }
    }

    // 2. Update task
    private void editTask () {
        System.out.println( "\n" + "=".repeat(30) );
        System.out.println( "\n\t UPDATE TASK: " );
        System.out.println( "\n" + "=".repeat(30) );

        try {
            scanner.nextLine();

            JSONObject data = loadData();
            JSONArray tasks = data.getJSONArray( "tasks" );

            if (tasks.isEmpty()) {
                System.out.println( "No tasks found for editing" );
                return;
            }

            System.out.println( "Task found for editing: " );
            for ( int i = 0; i < tasks.length(); i++ ) {
                JSONObject task = tasks.getJSONObject( i );
                System.out.println( "--> ID: " + task.getInt( "id" ) + " - " + task.getString( "description" ) );
            }

            System.out.print( "Enter the task ID to edit: " );
            int id = readOption();

            JSONObject taskFound = null;
            for ( int i = 0; i < tasks.length(); i++ ) {
                JSONObject task = tasks.getJSONObject( i );
                if ( task.getInt( "id" ) == id ) {
                    taskFound = task;
                    break;
                }
            }

            if ( taskFound == null ) {
                System.out.println( "\nTask with ID " + id + " not found!" );
                return;
            }

            System.out.println( "Current task data: " );
            System.out.println( "Description: " + taskFound.getString( "description" ) );
            System.out.println( "Status: " + taskFound.getString( "status" ) );

            System.out.println( "Enter new data (Enter to keep current)" );

            System.out.print( "Enter new description: " );
            String newDescription = scanner.nextLine().trim();
            if ( !newDescription.isEmpty() ) {
                taskFound.put( "description", newDescription );
            }

            String taskStatusUpdated = taskFound.getString( "status" );

            System.out.print( "Enter new status - todo, in-progress or done: " );
            String newStatus = scanner.nextLine().trim();
            if ( newStatus.isEmpty() ) {
                taskFound.put( "status", taskStatusUpdated );
            } else if ( newStatus.equalsIgnoreCase( "todo" ) || newStatus.equalsIgnoreCase( "in-progress" ) || newStatus.equalsIgnoreCase( "done" ) ) {
                taskFound.put( "status", newStatus );
            } else {
                System.out.println( "Invalid status!" );
                return;
            }

            String updatedAt = LocalDateTime.now().format( formatter );
            taskFound.put( "updatedAt", updatedAt );

            saveData( data );

            System.out.println( "\n\t DONE!" );

        } catch ( Exception e ) {
            System.err.println( "Error updating task: " + e.getMessage() );
        }
    }

    // 3. Delete task
    private void deleteTask () {
        System.out.println( "\n" + "=".repeat(30) );
        System.out.println( "\n\t DELETE TASK: " );
        System.out.println( "\n" + "=".repeat(30) );

        try {

            scanner.nextLine();

            JSONObject data = loadData();
            JSONArray tasks = data.getJSONArray( "tasks" );

            if (tasks.isEmpty()) {
                System.out.println( "No tasks found for deleting" );
                return;
            }

            System.out.println( "Task found for deleting: " );
            for ( int i = 0; i < tasks.length(); i++ ) {
                JSONObject task = tasks.getJSONObject( i );
                System.out.println( "--> Task: " + task.getInt( "id" ) +
                        " | Description: " + task.getString( "description" ) +
                        " | Status: " + task.getString( "status" ) +
                        " | Created at: " + task.getString( "createdAt" ) +
                        " | Updated at: " + task.getString( "updatedAt" ) );
            }

            System.out.print( "Enter the task ID to delete: " );
            int id = readOption();

            boolean deleted = false;
            for ( int i = 0; i < tasks.length(); i++ ) {
                JSONObject task = tasks.getJSONObject( i );
                if ( task.getInt( "id" ) == id ) {

                    System.out.println( "Task to be deleted: " + task.getString( "description" ) );

                    System.out.print( "\nATENTION! You are about to delete the task: " + task.getString( "description" ) + ". You confirm? ( Y or N ): " );
                    String confirm = scanner.nextLine().trim().toLowerCase();

                    if ( confirm.equalsIgnoreCase( "Y" ) ) {
                        tasks.remove( i );
                        deleted = true;
                    } else if ( confirm.equalsIgnoreCase( "N" ) ) {
                        System.out.println( "Deletion canceled!" );
                        return;
                    }
                    break;

                }
            }

            if ( deleted ) {
                saveData( data );
                System.out.println( "\n\t DONE!" );
            } else {
                System.out.println( "Task not found!." );
            }

        } catch ( Exception e ) {
            System.err.println( "Error deleting task: " + e.getMessage() );
        }
    }

    // 4. List all tasks
    private void listAllTasks () {
        System.out.println( "\n" + "=".repeat(30) );
        System.out.println( "\n\tALL TASKS" );
        System.out.println( "\n" + "=".repeat(30) );

        try {

            JSONObject data = loadData();
            JSONArray tasks = data.getJSONArray( "tasks" );

            if ( tasks.isEmpty() ) {
                System.out.println( "Tasks not found!." );
                System.out.println( "Use option two to create a task!" );
                return;
            }

            for ( int i = 0; i < tasks.length(); i++ ) {
                JSONObject task = tasks.getJSONObject( i );

                int id = task.getInt( "id" );
                String description = task.getString( "description" );
                String status = task.getString( "status" );
                String createdAt = task.getString( "createdAt" );
                String updatedAt = task.getString( "updatedAt" );

                System.out.println( "--> Task: " + id + " | Description: " + description + " | Status: " + status + " | Created at: " + createdAt + " | Updated at: " + updatedAt );
            }

            System.out.println( "\nTasks: " + tasks.length() );

        } catch (Exception e) {
            System.out.println( "Error listing tasks: " + e.getMessage() );
        }
    }

    // 5. List all tasks that are done
    private void listAllTasksDone () {
        System.out.println( "\n" + "=".repeat(30) );
        System.out.println( "\n\tALL TASKS DONE" );
        System.out.println( "\n" + "=".repeat(30) );

        int totalDone = 0;

        try {
            JSONObject data = loadData();
            JSONArray tasks = data.getJSONArray( "tasks" );

            for ( int i = 0; i < tasks.length(); i++ ) {
                JSONObject task = tasks.getJSONObject( i );

                String currentStatus = task.getString( "status" ).toLowerCase();

                if ( currentStatus.equals( "done" ) ) {
                    int id = task.getInt( "id" );
                    String description = task.getString( "description" );
                    String status = task.getString( "status" );
                    String createdAt = task.getString( "createdAt" );
                    String updatedAt = task.getString( "updatedAt" );

                    System.out.println( "--> Task: " + id + " | Description: " + description + " | Status: " + status + " | Created at: " + createdAt + " | Updated at: " + updatedAt );

                    totalDone++;
                }
            }

            if ( totalDone == 0 ) {
                System.out.println( "\nNo tasks done." );
            } else {
                System.out.println( "\nTasks done: " + totalDone );
            }

        } catch (Exception e) {
            System.out.println( "Error listing tasks done: " + e.getMessage() );
        }
    }

    // 6. List all tasks that are done
    private void listAllTasksNotDone () {
        System.out.println( "\n" + "=".repeat(30) );
        System.out.println( "\n\tALL TASKS NOT DONE" );
        System.out.println( "\n" + "=".repeat(30) );

        int totalNotDone = 0;

        try {
            JSONObject data = loadData();
            JSONArray tasks = data.getJSONArray( "tasks" );

            for ( int i = 0; i < tasks.length(); i++ ) {
                JSONObject task = tasks.getJSONObject( i );

                String currentStatus = task.getString( "status" ).toLowerCase();

                if ( currentStatus.equals( "todo" ) ) {
                    int id = task.getInt( "id" );
                    String description = task.getString( "description" );
                    String status = task.getString( "status" );
                    String createdAt = task.getString( "createdAt" );
                    String updatedAt = task.getString( "updatedAt" );

                    System.out.println( "--> Task: " + id + " | Description: " + description + " | Status: " + status + " | Created at: " + createdAt + " | Updated at: " + updatedAt );

                    totalNotDone++;
                }
            }

            if ( totalNotDone == 0 ) {
                System.out.println( "\nNo tasks not done." );
            } else {
                System.out.println( "\nTasks not done: " + totalNotDone );
            }

        } catch (Exception e) {
            System.out.println( "Error listing tasks not done: " + e.getMessage() );
        }
    }

    // 7. List all tasks that are in progress
    private void listAllTasksInProgress () {
        System.out.println( "\n" + "=".repeat(30) );
        System.out.println( "\n\tALL TASKS IN PROGRESSS" );
        System.out.println( "\n" + "=".repeat(30) );

        int totalInProgress = 0;

        try {
            JSONObject data = loadData();
            JSONArray tasks = data.getJSONArray( "tasks" );

            for ( int i = 0; i < tasks.length(); i++ ) {
                JSONObject task = tasks.getJSONObject( i );

                String currentStatus = task.getString( "status" ).toLowerCase();

                if ( currentStatus.equals( "in-progress" ) ) {
                    int id = task.getInt( "id" );
                    String description = task.getString( "description" );
                    String status = task.getString( "status" );
                    String createdAt = task.getString( "createdAt" );
                    String updatedAt = task.getString( "updatedAt" );

                    System.out.println( "--> Task: " + id + " | Description: " + description + " | Status: " + status + " | Created at: " + createdAt + " | Updated at: " + updatedAt );

                    totalInProgress++;
                }
            }

            if ( totalInProgress == 0 ) {
                System.out.println( "\nNo tasks in progress." );
            } else {
                System.out.println( "\nTasks in progress: " + totalInProgress );
            }

        } catch (Exception e) {
            System.out.println( "Error listing tasks not done: " + e.getMessage() );
        }
    }

    // Read option from user
    private int readOption () {
        try {
            return Integer.parseInt( scanner.nextLine().trim() );
        } catch ( NumberFormatException e ) {
            return -1;
        }
    }

    // =======================
    // == Auxiliary methods ==
    // =======================

    private void initializeFile () {
        File file = new File( FILE );
        if ( !file.exists() ) {
            try {
                JSONObject data = new JSONObject();
                data.put( "tasks", new JSONArray() );
                saveData( data );
            } catch ( Exception e ) {
                System.err.println( "Error creating file: " + e.getMessage() );
            }
        }
    }

    private JSONObject loadData () throws IOException {
        String content = new String( Files.readAllBytes( Paths.get( FILE ) ) );
        return new JSONObject( content );
    }

    private int generateNextID ( JSONArray tasks ) {
        int biggerID = 0;
        for ( int i = 0; i < tasks.length(); i++ ) {
            int id = tasks.getJSONObject( i ).getInt( "id" );
            if ( id > biggerID ) {
                biggerID = id;
            }
        }
        return biggerID + 1;
    }

    private void saveData ( JSONObject data ) throws Exception {
        try (FileWriter writer = new FileWriter( FILE )) {
            writer.write( data.toString(2) );
        }
    }

}

