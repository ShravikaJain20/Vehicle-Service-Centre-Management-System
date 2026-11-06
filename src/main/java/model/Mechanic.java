package model;

import java.sql.Timestamp;

public class Mechanic {
    private int mechanicId;
    private String name;
    private String phone;
    private String specialization;
    private String status; // 'Available', 'Busy', 'On Leave'
    private int activeJobsCount;
    private Timestamp createdAt;

    public Mechanic() {}

    public Mechanic(int mechanicId, String name, String phone, String specialization, String status, int activeJobsCount, Timestamp createdAt) {
        this.mechanicId = mechanicId;
        this.name = name;
        this.phone = phone;
        this.specialization = specialization;
        this.status = status;
        this.activeJobsCount = activeJobsCount;
        this.createdAt = createdAt;
    }

    public int getMechanicId() { return mechanicId; }
    public void setMechanicId(int mechanicId) { this.mechanicId = mechanicId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getActiveJobsCount() { return activeJobsCount; }
    public void setActiveJobsCount(int activeJobsCount) { this.activeJobsCount = activeJobsCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + specialization + " - " + status + ")";
    }
}
