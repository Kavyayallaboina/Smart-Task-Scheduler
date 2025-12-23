import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 Smart Task Scheduler
 Single Java file
 Beginner friendly
*/

public class SmartTaskScheduler extends JFrame {

    // ---------- Task Class ----------
    static class Task implements Serializable, Comparable<Task> {
        String title;
        int priority; // 1=High, 2=Medium, 3=Low
        String deadline; // dd-MM-yyyy

        Task(String title, int priority, String deadline) {
            this.title = title;
            this.priority = priority;
            this.deadline = deadline;
        }

        @Override
        public int compareTo(Task t) {
            return Integer.compare(this.priority, t.priority);
        }

        @Override
        public String toString() {
            String p = priority == 1 ? "High" : priority == 2 ? "Medium" : "Low";
            return title + " | " + p + " | " + deadline;
        }
    }

    // ---------- Variables ----------
    PriorityQueue<Task> tasks = new PriorityQueue<>();
    DefaultListModel<Task> model = new DefaultListModel<>();
    JList<Task> list = new JList<>(model);

    JTextField titleField = new JTextField();
    JTextField dateField = new JTextField("dd-MM-yyyy");

    JComboBox<String> priorityBox =
            new JComboBox<>(new String[]{"High", "Medium", "Low"});

    JComboBox<String> filterBox =
            new JComboBox<>(new String[]{"All Tasks", "High Priority", "Today's Tasks"});

    File file = new File("tasks.dat");
    java.util.Timer reminderTimer;

    // ---------- Constructor ----------
    public SmartTaskScheduler() {
        setTitle("Smart Task Scheduler");
        setSize(550, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel top = new JPanel(new GridLayout(5, 2, 5, 5));
        top.setBorder(BorderFactory.createTitledBorder("Add Task"));

        top.add(new JLabel("Task Title:"));
        top.add(titleField);

        top.add(new JLabel("Priority:"));
        top.add(priorityBox);

        top.add(new JLabel("Deadline:"));
        top.add(dateField);

        JButton addBtn = new JButton("Add Task");
        JButton delBtn = new JButton("Delete Task");

        top.add(addBtn);
        top.add(delBtn);

        top.add(new JLabel("Filter:"));
        top.add(filterBox);

        add(top, BorderLayout.NORTH);

        // Center
        add(new JScrollPane(list), BorderLayout.CENTER);

        // Actions
        addBtn.addActionListener(e -> addTask());
        delBtn.addActionListener(e -> deleteTask());
        filterBox.addActionListener(e -> refreshList());

        loadTasks();
        refreshList();
        startReminder();

        setVisible(true);
    }

    // ---------- Methods ----------
    void addTask() {
        String title = titleField.getText();
        String date = dateField.getText();
        int priority = priorityBox.getSelectedIndex() + 1;

        if (title.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields!");
            return;
        }

        tasks.add(new Task(title, priority, date));
        saveTasks();
        refreshList();

        titleField.setText("");
        dateField.setText("dd-MM-yyyy");
    }

    void deleteTask() {
        Task t = list.getSelectedValue();
        if (t != null) {
            tasks.remove(t);
            saveTasks();
            refreshList();
        }
    }

    void refreshList() {
        model.clear();
        String filter = (String) filterBox.getSelectedItem();
        String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

        PriorityQueue<Task> temp = new PriorityQueue<>(tasks);
        while (!temp.isEmpty()) {
            Task t = temp.poll();

            if (filter.equals("High Priority") && t.priority != 1) continue;
            if (filter.equals("Today's Tasks") && !t.deadline.equals(today)) continue;

            model.addElement(t);
        }
    }

    void saveTasks() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new ArrayList<>(tasks));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadTasks() {
        if (!file.exists()) return;
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(file))) {
            tasks.addAll((ArrayList<Task>) ois.readObject());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- Reminder ----------
    void startReminder() {
        reminderTimer = new java.util.Timer();
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                for (Task t : tasks) {
                    if (t.deadline.equals(today)) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Reminder: " + t.title,
                                "Task Reminder",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        break;
                    }
                }
            }
        }, 0, 60000); // every 1 minute
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartTaskScheduler::new);
    }
}