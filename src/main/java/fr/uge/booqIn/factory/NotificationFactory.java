package fr.uge.booqIn.factory;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.BookWaitingQueue;
import fr.uge.booqIn.entity.Notification;
import fr.uge.booqIn.entity.User;
import org.springframework.stereotype.Component;

/**
 * Factory for creating notifications with appropriate messages
 */
@Component
public class NotificationFactory {

    /**
     * Creates a book availability notification
     */
    public Notification createBookAvailableNotification(Long userId, String bookTitle) {
        String message = "Book Available: " + bookTitle + " is now available for borrowing.";
        return new Notification(userId, message);
    }

    /**
     * Creates a notification about being added to the waiting queue
     */
    public Notification createQueueJoinedNotification(Long userId, String bookTitle, int position) {
        String message = "Waiting List: You've been added to the waiting list for \"" +
                bookTitle + "\". Current position: #" + position;
        return new Notification(userId, message);
    }

    /**
     * Creates a notification about moving up in the queue
     */
    public Notification createQueuePositionUpdateNotification(Long userId, BookWaitingQueue queue, int newPosition) {
        String message = "Waiting List Update: Your position for \"" +
                queue.getBook().getTitle() + "\" has changed to #" + newPosition;
        return new Notification(userId, message);
    }

    /**
     * Creates a notification when a book becomes available for a queued user
     */
    public Notification createQueueBookAvailableNotification(Long userId, String bookTitle) {
        String message = "Your Turn: " + bookTitle +
                " is now available for you to borrow! You have 1 hour to reserve it before it's offered to the next person in line.";
        return new Notification(userId, message);
    }

    public Notification createQueueCancelledNotification(Long userId, String bookTitle) {
        String message = "Queue Cancelled: Your reservation for \"" +
                bookTitle + "\" has been cancelled.";
        return new Notification(userId, message);
    }

    /**
     * Creates a notification when a queue reservation expires
     */
    public Notification createQueueExpirationNotification(Long userId, String bookTitle) {
        String message = "Reservation Expired: Your reservation window for \"" +
                bookTitle + "\" has expired. If you're still interested, please rejoin the waiting list.";
        return new Notification(userId, message);
    }

    /**
     * Creates a book shipped notification
     */
    public Notification createBookShippedNotification(Long userId, BookLending lending) {
        String message = "Book Shipped: " + lending.getBook().getTitle() +
                " has been shipped to you by " + lending.getLender().getFullName() + ".";
        return new Notification(userId, message);
    }

    /**
     * Creates a book due soon notification
     */
    public Notification createBookDueSoonNotification(Long userId, BookLending lending) {
        String message = "Book Due Soon: " + lending.getBook().getTitle() +
                " is due for return in 3 days.";
        return new Notification(userId, message);
    }

    /**
     * Creates a book overdue notification
     */
    public Notification createBookOverdueNotification(Long userId, BookLending lending) {
        String message = "Book Overdue: " + lending.getBook().getTitle() +
                " was due on " + lending.getDueDate() + ". Please return it soon.";
        return new Notification(userId, message);
    }

    /**
     * Creates a book returned notification
     */
    public Notification createBookReturnedNotification(Long userId, BookLending lending) {
        String message = "Book Returned: " + lending.getBook().getTitle() +
                " has been returned by " + lending.getBorrower().getFullName() + ".";
        return new Notification(userId, message);
    }

    /**
     * Creates a new comment notification
     */
    public Notification createCommentNotification(Long userId, User commenter, String contentType, String contentTitle) {
        String message = "New Comment: " + commenter.getFullName() +
                " commented on " + contentType + ": " + contentTitle;
        return new Notification(userId, message);
    }

    /**
     * Creates a new follower notification
     */
    public Notification createFollowNotification(Long userId, User follower) {
        String message = "New Follower: " + follower.getFullName() + " is now following you.";
        return new Notification(userId, message);
    }

    /**
     * Creates a vote notification
     */
    public Notification createVoteNotification(Long userId, User voter, String contentType, String contentTitle) {
        String message = "New Vote: " + voter.getFullName() + " voted on your " +
                contentType + ": " + contentTitle;
        return new Notification(userId, message);
    }
}