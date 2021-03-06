package service;

import model.Customer;
import model.IRoom;
import model.Reservation;

import java.util.*;
import java.util.stream.Collectors;

public class ReservationService {

    private static final ReservationService SINGLETON = new ReservationService();

    private static final int RECOMMENDED_ROOMS_DEFAULT_PLUS_DAYS = 7; // not required in the spec


    private ReservationService() {}

    public static ReservationService getSingleton() {
        return SINGLETON;
    }

    private final Map<String, IRoom> rooms = new HashMap<>();  // what purpose?
    private final Map<String, Collection<Reservation>> reservations = new HashMap<>(); // what purpose?

    public void addRoom(IRoom room){
        rooms.put(room.getRoomNumber(),room);
    }

    public IRoom getARoom(String roomId){
        return rooms.get(roomId);
    }

    public Collection<IRoom> getAllRooms() {
        return rooms.values();  //得到一个集合，装的是一个个Room类的对象，type是IRoom
    }


    public Reservation reserveARoom(Customer customer, IRoom room, Date checkInDate, Date checkOutDate){
        final Reservation reservation = new Reservation(customer, room, checkInDate, checkOutDate);

        Collection<Reservation> customerReservations = getCustomersReservation(customer);

        if (customerReservations == null) {
            customerReservations = new LinkedList<>();
        }

        customerReservations.add(reservation);
        reservations.put(customer.getEmail(), customerReservations);

        return reservation;
    }

    public Collection<IRoom> findRooms(Date checkInDate, Date checkOutDate){
        return findAvailableRooms(checkInDate, checkOutDate);
    }

    public Collection<Reservation> getCustomersReservation(Customer customer){
        return reservations.get(customer.getEmail());
    }

    public void printAllReservation(){
        final Collection<Reservation> reservations = getAllReservations();

        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
        } else {
            for (Reservation reservation : reservations) {
                System.out.println(reservation + "\n");
            }
        }
    }

    private Collection<Reservation> getAllReservations() {
        final Collection<Reservation> allReservations = new LinkedList<>();

        for(Collection<Reservation> reservations : reservations.values()) {
            allReservations.addAll(reservations);
        }

        return allReservations;
    }

    public Collection<IRoom> findAlternativeRooms(final Date checkInDate, final Date checkOutDate) {
        return findAvailableRooms(addDefaultPlusDays(checkInDate), addDefaultPlusDays(checkOutDate));
    }

    public Date addDefaultPlusDays(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, RECOMMENDED_ROOMS_DEFAULT_PLUS_DAYS);

        return calendar.getTime();
    }


    private Collection<IRoom> findAvailableRooms(final Date checkInDate, final Date checkOutDate) {
        final Collection<Reservation> allReservations = getAllReservations();
        final Collection<IRoom> notAvailableRooms = new LinkedList<>();

        for (Reservation reservation : allReservations) {
            if (reservationOverlaps(reservation, checkInDate, checkOutDate)) {
                notAvailableRooms.add(reservation.getRoom());
            }
        }

        return rooms.values().stream().filter(room -> notAvailableRooms.stream()
                        .noneMatch(notAvailableRoom -> notAvailableRoom.equals(room)))
                .collect(Collectors.toList());
    }

    private boolean reservationOverlaps(final Reservation reservation, final Date checkInDate,
                                        final Date checkOutDate){
        return checkInDate.before(reservation.getCheckOutDate())
                && checkOutDate.after(reservation.getCheckInDate());
    }




}
