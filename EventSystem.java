import java.util.*;

// ================================================================
//  EVENT CREATION & REGISTRATION SYSTEM
//  Uses: Hash Table, Linked List, Queue, Stack, Binary Search,
//        Sorting (Merge Sort), Priority Queue (Min-Heap)
// ================================================================

// ──────────────────────────────────────────
// MODEL: Person
// ──────────────────────────────────────────
class Person {
    int    id;
    String name;
    String email;
    int    priority; // 1 = VIP, 5 = General

    Person(int id, String name, String email, int priority) {
        this.id       = id;
        this.name     = name;
        this.email    = email;
        this.priority = priority;
    }

    public String toString() {
        String tag = priority == 1 ? "[VIP]" : "[GEN]";
        return tag + " ID:" + id + "  Name:" + name + "  Email:" + email;
    }
}

// ──────────────────────────────────────────
// MODEL: Event
// ──────────────────────────────────────────
class Event {
    int    id;
    String name;
    String date;
    String venue;
    int    capacity;

    Event(int id, String name, String date, String venue, int capacity) {
        this.id       = id;
        this.name     = name;
        this.date     = date;
        this.venue    = venue;
        this.capacity = capacity;
    }

    public String toString() {
        return "ID:" + id + "  \"" + name + "\"  Date:" + date +
               "  Venue:" + venue + "  Capacity:" + capacity;
    }
}

// ──────────────────────────────────────────
// SINGLY LINKED LIST  (stores registered persons per event)
// ──────────────────────────────────────────
class PersonList {

    private static class Node {
        Person data;
        Node   next;
        Node(Person p) { data = p; }
    }

    private Node head;
    private int  size;

    public void add(Person p) {
        Node n = new Node(p);
        if (head == null) { head = n; }
        else {
            Node cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = n;
        }
        size++;
    }

    public boolean contains(int personId) {
        Node cur = head;
        while (cur != null) {
            if (cur.data.id == personId) return true;
            cur = cur.next;
        }
        return false;
    }

    public List<Person> toList() {
        List<Person> out = new ArrayList<Person>();
        Node cur = head;
        while (cur != null) { out.add(cur.data); cur = cur.next; }
        return out;
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }
}

// ──────────────────────────────────────────
// LINKED QUEUE  (waitlist per event)
// ──────────────────────────────────────────
class WaitlistQueue {

    private static class QNode {
        Person data;
        QNode  next;
        QNode(Person p) { data = p; }
    }

    private QNode front, rear;
    private int   size;

    public void enqueue(Person p) {
        QNode n = new QNode(p);
        if (rear == null) { front = rear = n; }
        else { rear.next = n; rear = n; }
        size++;
    }

    public Person dequeue() {
        if (front == null) throw new NoSuchElementException();
        Person p = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return p;
    }

    public boolean isEmpty() { return front == null; }
    public int size()        { return size; }

    public List<Person> toList() {
        List<Person> out = new ArrayList<Person>();
        QNode cur = front;
        while (cur != null) { out.add(cur.data); cur = cur.next; }
        return out;
    }
}

// ──────────────────────────────────────────
// LINKED STACK  (undo log)
// ──────────────────────────────────────────
class ActionStack {

    private static class SNode {
        String data;
        SNode  below;
        SNode(String d) { data = d; }
    }

    private SNode top;

    public void push(String action) {
        SNode n = new SNode(action);
        n.below = top;
        top = n;
    }

    public String pop() {
        if (top == null) return null;
        String d = top.data;
        top = top.below;
        return d;
    }

    public boolean isEmpty() { return top == null; }
}

// ──────────────────────────────────────────
// MIN-HEAP  (priority boarding order)
// ──────────────────────────────────────────
class PriorityBoardingHeap {

    private final List<Person> heap = new ArrayList<Person>();

    private int parent(int i) { return (i - 1) / 2; }
    private int left(int i)   { return 2 * i + 1;   }
    private int right(int i)  { return 2 * i + 2;   }

    private void swap(int a, int b) {
        Person t = heap.get(a); heap.set(a, heap.get(b)); heap.set(b, t);
    }

    public void insert(Person p) {
        heap.add(p);
        int i = heap.size() - 1;
        while (i > 0 && heap.get(parent(i)).priority > heap.get(i).priority) {
            swap(i, parent(i)); i = parent(i);
        }
    }

    public Person extractMin() {
        if (heap.isEmpty()) return null;
        Person min = heap.get(0);
        heap.set(0, heap.get(heap.size() - 1));
        heap.remove(heap.size() - 1);
        siftDown(0);
        return min;
    }

    private void siftDown(int i) {
        int s = i, l = left(i), r = right(i);
        if (l < heap.size() && heap.get(l).priority < heap.get(s).priority) s = l;
        if (r < heap.size() && heap.get(r).priority < heap.get(s).priority) s = r;
        if (s != i) { swap(i, s); siftDown(s); }
    }

    public boolean isEmpty() { return heap.isEmpty(); }
}

// ──────────────────────────────────────────
// HASH TABLE  (event store — separate chaining)
// ──────────────────────────────────────────
class EventHashTable {

    private static class Entry {
        int   key;
        Event value;
        Entry next;
        Entry(int k, Event v) { key = k; value = v; }
    }

    private final Entry[] buckets;
    private final int     cap;
    private int           size;

    EventHashTable(int cap) {
        this.cap = cap;
        buckets  = new Entry[cap];
    }

    private int hash(int key) { return key % cap; }

    public void put(int key, Event ev) {
        int idx = hash(key);
        Entry cur = buckets[idx];
        while (cur != null) {
            if (cur.key == key) { cur.value = ev; return; }
            cur = cur.next;
        }
        Entry n = new Entry(key, ev);
        n.next = buckets[idx];
        buckets[idx] = n;
        size++;
    }

    public Event get(int key) {
        Entry cur = buckets[hash(key)];
        while (cur != null) {
            if (cur.key == key) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    public List<Event> allEvents() {
        List<Event> out = new ArrayList<Event>();
        for (Entry b : buckets) {
            Entry cur = b;
            while (cur != null) { out.add(cur.value); cur = cur.next; }
        }
        return out;
    }

    public int size() { return size; }
}

// ──────────────────────────────────────────
// MERGE SORT  (sort events by name)
// ──────────────────────────────────────────
class MergeSort {

    public static List<Event> sortByName(List<Event> list) {
        if (list.size() <= 1) return list;
        int mid = list.size() / 2;
        List<Event> left  = sortByName(new ArrayList<Event>(list.subList(0, mid)));
        List<Event> right = sortByName(new ArrayList<Event>(list.subList(mid, list.size())));
        return merge(left, right);
    }

    private static List<Event> merge(List<Event> l, List<Event> r) {
        List<Event> out = new ArrayList<Event>();
        int i = 0, j = 0;
        while (i < l.size() && j < r.size()) {
            if (l.get(i).name.compareToIgnoreCase(r.get(j).name) <= 0) out.add(l.get(i++));
            else                                                         out.add(r.get(j++));
        }
        while (i < l.size()) out.add(l.get(i++));
        while (j < r.size()) out.add(r.get(j++));
        return out;
    }
}

// ──────────────────────────────────────────
// BINARY SEARCH  (find event by ID in sorted array)
// ──────────────────────────────────────────
class BinarySearch {

    public static Event findById(List<Event> sorted, int id) {
        int lo = 0, hi = sorted.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int eid = sorted.get(mid).id;
            if      (eid == id) return sorted.get(mid);
            else if (eid <  id) lo = mid + 1;
            else                hi = mid - 1;
        }
        return null;
    }
}

// ──────────────────────────────────────────
// MAIN SYSTEM
// ──────────────────────────────────────────
public class EventSystem {

    private static final EventHashTable                        eventTable   = new EventHashTable(16);
    private static final Map<Integer, PersonList>              registered   = new HashMap<Integer, PersonList>();
    private static final Map<Integer, WaitlistQueue>           waitlists    = new HashMap<Integer, WaitlistQueue>();
    private static final Map<Integer, Person>                  personStore  = new HashMap<Integer, Person>();
    private static final ActionStack                           undoStack    = new ActionStack();
    private static final Scanner                               sc           = new Scanner(System.in);

    private static int eventIdCounter  = 1;
    private static int personIdCounter = 1;

    // ── Helpers ──────────────────────────────────────
    static void line()         { System.out.println("--------------------------------------------------"); }
    static void header(String t) {
        System.out.println("\n==================================================");
        System.out.println("  " + t);
        System.out.println("==================================================");
    }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  [!] Please enter a valid number."); }
        }
    }

    static String readStr(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // ── 1. CREATE EVENT ───────────────────────────────
    static void createEvent() {
        header("CREATE NEW EVENT");
        String name     = readStr("  Event name   : ");
        String date     = readStr("  Date (DD/MM/YYYY): ");
        String venue    = readStr("  Venue        : ");
        int    capacity = readInt("  Max capacity : ");

        if (name.isEmpty() || venue.isEmpty()) {
            System.out.println("  [!] Name and venue cannot be empty."); return;
        }
        if (capacity <= 0) {
            System.out.println("  [!] Capacity must be greater than 0."); return;
        }

        Event ev = new Event(eventIdCounter++, name, date, venue, capacity);
        eventTable.put(ev.id, ev);
        registered.put(ev.id, new PersonList());
        waitlists .put(ev.id, new WaitlistQueue());
        undoStack.push("CREATED_EVENT:" + ev.id + ":" + ev.name);

        System.out.println("\n  [SUCCESS] Event created!");
        System.out.println("  " + ev);
    }

    // ── 2. VIEW ALL EVENTS ────────────────────────────
    static void viewAllEvents() {
        header("ALL EVENTS  (sorted A-Z by name)");
        List<Event> all = eventTable.allEvents();
        if (all.isEmpty()) { System.out.println("  No events found."); return; }
        List<Event> sorted = MergeSort.sortByName(all);
        for (int i = 0; i < sorted.size(); i++) {
            Event ev  = sorted.get(i);
            int   reg = registered.get(ev.id) != null ? registered.get(ev.id).size() : 0;
            System.out.println("  " + (i+1) + ". " + ev + "  [Registered:" + reg + "]");
        }
    }

    // ── 3. REGISTER FOR EVENT ─────────────────────────
    static void registerForEvent() {
        header("REGISTER FOR AN EVENT");

        // Show available events first
        List<Event> all = eventTable.allEvents();
        if (all.isEmpty()) { System.out.println("  No events available. Create one first."); return; }

        System.out.println("  Available Events:");
        List<Event> sorted = MergeSort.sortByName(all);
        for (int i = 0; i < sorted.size(); i++) {
            Event ev  = sorted.get(i);
            int   reg = registered.get(ev.id).size();
            int   wl  = waitlists .get(ev.id).size();
            String status = reg < ev.capacity ? "OPEN (" + (ev.capacity - reg) + " spots left)"
                                              : "FULL (waitlist: " + wl + ")";
            System.out.println("  " + (i+1) + ". [ID:" + ev.id + "] " + ev.name
                               + "  " + ev.date + "  @ " + ev.venue + "  -> " + status);
        }

        int eid = readInt("\n  Enter Event ID to register for: ");
        Event ev = eventTable.get(eid);
        if (ev == null) { System.out.println("  [!] Event ID not found."); return; }

        // Person details
        line();
        System.out.println("  Enter your details:");
        String name  = readStr("  Your name  : ");
        String email = readStr("  Your email : ");
        if (name.isEmpty() || email.isEmpty()) {
            System.out.println("  [!] Name and email cannot be empty."); return;
        }
        int vip = readInt("  Priority (1=VIP, 2=3=4, 5=General): ");
        if (vip < 1 || vip > 5) vip = 5;

        Person p = new Person(personIdCounter++, name, email, vip);
        personStore.put(p.id, p);

        PersonList    roll = registered.get(eid);
        WaitlistQueue wl   = waitlists .get(eid);

        // Check duplicate
        if (roll.contains(p.id)) {
            System.out.println("  [!] Already registered."); return;
        }

        if (roll.size() < ev.capacity) {
            roll.add(p);
            undoStack.push("REGISTERED:" + p.id + ":" + p.name + ":EVENT:" + eid);
            System.out.println("\n  [SUCCESS] Registration confirmed!");
            System.out.println("  Person  : " + p);
            System.out.println("  Event   : " + ev.name + "  on " + ev.date + "  @ " + ev.venue);
            System.out.println("  Seat    : " + roll.size() + " of " + ev.capacity);
        } else {
            wl.enqueue(p);
            undoStack.push("WAITLISTED:" + p.id + ":" + p.name + ":EVENT:" + eid);
            System.out.println("\n  [WAITLISTED] Event is full.");
            System.out.println("  You are #" + wl.size() + " on the waitlist for '" + ev.name + "'.");
            System.out.println("  Person  : " + p);
        }
    }

    // ── 4. VIEW REGISTRATIONS FOR AN EVENT ───────────
    static void viewRegistrations() {
        header("VIEW REGISTRATIONS");
        List<Event> all = eventTable.allEvents();
        if (all.isEmpty()) { System.out.println("  No events found."); return; }

        System.out.println("  Events:");
        for (Event ev : MergeSort.sortByName(all))
            System.out.println("  [ID:" + ev.id + "] " + ev.name);

        int eid = readInt("\n  Enter Event ID: ");
        Event ev = eventTable.get(eid);
        if (ev == null) { System.out.println("  [!] Event not found."); return; }

        PersonList    roll = registered.get(eid);
        WaitlistQueue wl   = waitlists .get(eid);

        line();
        System.out.println("  Event    : " + ev);
        System.out.println("  Enrolled : " + roll.size() + " / " + ev.capacity);
        System.out.println();

        if (roll.isEmpty()) {
            System.out.println("  No registrations yet.");
        } else {
            // Show registered persons sorted by name using merge sort
            List<Person> persons = roll.toList();
            mergeSortPersons(persons);
            System.out.println("  Registered Attendees (sorted by name):");
            for (int i = 0; i < persons.size(); i++)
                System.out.println("  " + (i+1) + ". " + persons.get(i));
        }

        if (!wl.isEmpty()) {
            System.out.println("\n  Waitlist (" + wl.size() + " pending):");
            List<Person> wlList = wl.toList();
            for (int i = 0; i < wlList.size(); i++)
                System.out.println("  " + (i+1) + ". " + wlList.get(i));
        }

        // Show boarding order using Min-Heap
        if (!roll.isEmpty()) {
            System.out.println("\n  Priority Boarding Order (VIP first):");
            PriorityBoardingHeap heap = new PriorityBoardingHeap();
            for (Person p : roll.toList()) heap.insert(p);
            int rank = 1;
            while (!heap.isEmpty())
                System.out.println("  #" + rank++ + "  " + heap.extractMin());
        }
    }

    // ── 5. SEARCH EVENT BY ID ─────────────────────────
    static void searchEvent() {
        header("SEARCH EVENT BY ID");
        int id = readInt("  Enter Event ID: ");

        // Use binary search on ID-sorted list
        List<Event> all = eventTable.allEvents();
        all.sort(new Comparator<Event>() {
            public int compare(Event a, Event b) { return Integer.compare(a.id, b.id); }
        });

        Event found = BinarySearch.findById(all, id);
        if (found == null) {
            System.out.println("  [!] Event ID " + id + " not found.");
        } else {
            int reg = registered.get(found.id).size();
            int wl  = waitlists .get(found.id).size();
            System.out.println("\n  Event Found:");
            System.out.println("  " + found);
            System.out.println("  Registered: " + reg + " / " + found.capacity);
            System.out.println("  Waitlist  : " + wl);
        }
    }

    // ── 6. UNDO LAST ACTION ───────────────────────────
    static void undoLastAction() {
        header("UNDO LAST ACTION");
        String last = undoStack.pop();
        if (last == null) { System.out.println("  Nothing to undo."); return; }
        System.out.println("  Last action undone: " + last);
        System.out.println("  (Note: Data state reversal requires full undo support in prod.)");
    }

    // ── Merge sort persons by name ────────────────────
    static void mergeSortPersons(List<Person> list) {
        if (list.size() <= 1) return;
        int mid = list.size() / 2;
        List<Person> left  = new ArrayList<Person>(list.subList(0, mid));
        List<Person> right = new ArrayList<Person>(list.subList(mid, list.size()));
        mergeSortPersons(left); mergeSortPersons(right);
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).name.compareToIgnoreCase(right.get(j).name) <= 0) list.set(k++, left.get(i++));
            else                                                                list.set(k++, right.get(j++));
        }
        while (i < left.size())  list.set(k++, left.get(i++));
        while (j < right.size()) list.set(k++, right.get(j++));
    }

    // ── MAIN MENU ─────────────────────────────────────
    public static void main(String[] args) {

        System.out.println("==================================================");
        System.out.println("    EVENT CREATION & REGISTRATION SYSTEM");
        System.out.println("    Using: LinkedList, Queue, Stack, Heap,");
        System.out.println("           HashTable, MergeSort, BinarySearch");
        System.out.println("==================================================");

        while (true) {
            System.out.println("\n  MAIN MENU");
            System.out.println("  ---------");
            System.out.println("  1. Create Event");
            System.out.println("  2. View All Events");
            System.out.println("  3. Register for Event");
            System.out.println("  4. View Registrations for Event");
            System.out.println("  5. Search Event by ID");
            System.out.println("  6. Undo Last Action");
            System.out.println("  0. Exit");

            int choice = readInt("\n  Enter choice: ");

            switch (choice) {
                case 1: createEvent();        break;
                case 2: viewAllEvents();      break;
                case 3: registerForEvent();   break;
                case 4: viewRegistrations();  break;
                case 5: searchEvent();        break;
                case 6: undoLastAction();     break;
                case 0:
                    System.out.println("\n  Goodbye!");
                    return;
                default:
                    System.out.println("  [!] Invalid choice. Try again.");
            }
        }
    }
}