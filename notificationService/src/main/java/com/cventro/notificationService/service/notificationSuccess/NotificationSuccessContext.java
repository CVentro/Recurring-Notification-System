package com.cventro.notificationService.service.notificationSuccess;

public record NotificationSuccessContext(long sentCount, long maxCount) {

    public static NotificationSuccessContext empty() {
        return new NotificationSuccessContext(0, 0);
    }
}
