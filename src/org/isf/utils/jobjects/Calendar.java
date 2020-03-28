package org.isf.utils.jobjects;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

public abstract class Calendar extends JComponent {
    protected static final LocalTime START_TIME = LocalTime.of(9, 0);
    protected static final LocalTime END_TIME = LocalTime.of(17, 0);

    protected static final int MIN_WIDTH = 600;
    protected static final int MIN_HEIGHT = MIN_WIDTH;

    protected static final int HEADER_HEIGHT = 30;
    protected static final int TIME_COL_WIDTH = 100;

    // An estimate of the width of a single character (not exact but good
    // enough)
    private static final int FONT_LETTER_PIXEL_WIDTH = 7;
    private ArrayList<CalendarEvent> events;
    private double timeScale;
    private double dayWidth;
    private Graphics2D g2;

    private EventListenerList listenerList = new EventListenerList();

    public Calendar() {
        this(new ArrayList<CalendarEvent>());
    }

    Calendar(ArrayList<CalendarEvent> events) {
        this.events = events;
        setupEventListeners();
     
    }

    public static LocalTime roundTime(LocalTime time, int minutes) {
        LocalTime t = time;

        if (t.getMinute() % minutes > minutes / 2) {
            t = t.plusMinutes(minutes - (t.getMinute() % minutes));
        } else if (t.getMinute() % minutes < minutes / 2) {
            t = t.minusMinutes(t.getMinute() % minutes);
        }

        return t;
    }

    private void setupEventListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!checkCalendarEventClick(e.getPoint())) {
                    checkCalendarEmptyClick(e.getPoint());
                }
            }
        });
    }

    protected abstract boolean dateInRange(LocalDate date);

    private boolean checkCalendarEventClick(Point p) {
        double x0, x1, y0, y1;
        for (CalendarEvent event : events) {
            if (!dateInRange(event.getDate())) continue;

            x0 = dayToPixel(event.getDate().getDayOfWeek());
            y0 = timeToPixel(event.getStart());
            x1 = dayToPixel(event.getDate().getDayOfWeek()) + dayWidth;
            y1 = timeToPixel(event.getEnd());

       
        }
        return false;
    }

    private boolean checkCalendarEmptyClick(Point p) {
        final double x0 = dayToPixel(getStartDay());
        final double x1 = dayToPixel(getEndDay()) + dayWidth;
        final double y0 = timeToPixel(START_TIME);
        final double y1 = timeToPixel(END_TIME);

        if (p.getX() >= x0 && p.getX() <= x1 && p.getY() >= y0 && p.getY() <= y1) {
            LocalDate date = getDateFromDay(pixelToDay(p.getX()));
           
            return true;
        }
        return false;
    }

    protected abstract LocalDate getDateFromDay(DayOfWeek day);

    // CalendarEventClick methods





    // CalendarEmptyClick methods



  

 

    private void calculateScaleVars() {
        int width = getWidth();
        int height = getHeight();

        if (width < MIN_WIDTH) {
            width = MIN_WIDTH;
        }

        if (height < MIN_HEIGHT) {
            height = MIN_HEIGHT;
        }

        // Units are pixels per second
        timeScale = (double) (height - HEADER_HEIGHT) / (END_TIME.toSecondOfDay() - START_TIME.toSecondOfDay());
        dayWidth = (width - TIME_COL_WIDTH) / numDaysToShow();
    }

    protected abstract int numDaysToShow();

    // Gives x val of left most pixel for day col
    protected abstract double dayToPixel(DayOfWeek dayOfWeek);

    private double timeToPixel(LocalTime time) {
        return ((time.toSecondOfDay() - START_TIME.toSecondOfDay()) * timeScale) + HEADER_HEIGHT;
    }

    private LocalTime pixelToTime(double y) {
        return LocalTime.ofSecondOfDay((int) ((y - HEADER_HEIGHT) / timeScale) + START_TIME.toSecondOfDay()).truncatedTo(ChronoUnit.MINUTES);
    }

    private DayOfWeek pixelToDay(double x) {
        double pixel;
        DayOfWeek day;
        for (int i = getStartDay().getValue(); i <= getEndDay().getValue(); i++) {
            day = DayOfWeek.of(i);
            pixel = dayToPixel(day);
            if (x >= pixel && x < pixel + dayWidth) {
                return day;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        calculateScaleVars();
        g2 = (Graphics2D) g;

        // Rendering hints try to turn anti-aliasing on which improves quality
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set background to white
        g2.setColor(Color.white);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Set paint colour to black
        g2.setColor(Color.black);

        drawDayHeadings();
        drawTodayShade();
//        drawGrid();
//        drawTimes();
//        drawEvents();
//        drawCurrentTimeLine();
    }

    protected abstract DayOfWeek getStartDay();

    protected abstract DayOfWeek getEndDay();

    private void drawDayHeadings() {
        int y = 20;
        int x;
        LocalDate day;
        DayOfWeek dayOfWeek;

        for (int i = getStartDay().getValue(); i <= getEndDay().getValue(); i++) {
            dayOfWeek = DayOfWeek.of(i);
            day = getDateFromDay(dayOfWeek);

            String text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + day.getDayOfMonth() + "/" + day.getMonthValue();
            x = (int) (dayToPixel(DayOfWeek.of(i)) + (dayWidth / 2) - (FONT_LETTER_PIXEL_WIDTH * text.length() / 2));
            g2.drawString(text, x, y);
        }
    }

    private void drawGrid() {
        // Save the original colour
        final Color ORIG_COLOUR = g2.getColor();

        // Set colour to grey with half alpha (opacity)
        Color alphaGray = new Color(128, 128, 128, 128);
        Color alphaGrayLighter = new Color(200, 200, 200, 128);
        g2.setColor(alphaGray);

        // Draw vertical grid lines
        double x;
        for (int i = getStartDay().getValue(); i <= getEndDay().getValue(); i++) {
            x = dayToPixel(DayOfWeek.of(i));
            g2.draw(new Line2D.Double(x, HEADER_HEIGHT, x, timeToPixel(END_TIME)));
        }

        // Draw horizontal grid lines
        double y;
        int x1;
        for (LocalTime time = START_TIME; time.compareTo(END_TIME) <= 0; time = time.plusMinutes(30)) {
            y = timeToPixel(time);
            if (time.getMinute() == 0) {
                g2.setColor(alphaGray);
                x1 = 0;
            } else {
                g2.setColor(alphaGrayLighter);
                x1 = TIME_COL_WIDTH;
            }
            g2.draw(new Line2D.Double(x1, y, dayToPixel(getEndDay()) + dayWidth, y));
        }

        // Reset the graphics context's colour
        g2.setColor(ORIG_COLOUR);
    }

    private void drawTodayShade() {
        LocalDate today = LocalDate.now();

        // Check that date range being viewed is current date range
        if (!dateInRange(today)) return;

        final double x = dayToPixel(today.getDayOfWeek());
        final double y = timeToPixel(START_TIME);
        final double width = dayWidth;
        final double height = timeToPixel(END_TIME) - timeToPixel(START_TIME);

        final Color origColor = g2.getColor();
        Color alphaGray = new Color(200, 200, 200, 64);
        g2.setColor(alphaGray);
        g2.fill(new Rectangle2D.Double(x, y, width, height));
        g2.setColor(origColor);
    }

    private void drawCurrentTimeLine() {
        LocalDate today = LocalDate.now();

        // Check that date range being viewed is current date range
        if (!dateInRange(today)) return;

        final double x0 = dayToPixel(today.getDayOfWeek());
        final double x1 = dayToPixel(today.getDayOfWeek()) + dayWidth;
        final double y = timeToPixel(LocalTime.now());

        final Color origColor = g2.getColor();
        final Stroke origStroke = g2.getStroke();

        g2.setColor(new Color(255, 127, 110));
        g2.setStroke(new BasicStroke(2));
        g2.draw(new Line2D.Double(x0, y, x1, y));

        g2.setColor(origColor);
        g2.setStroke(origStroke);
    }

    private void drawTimes() {
        int y;
        for (LocalTime time = START_TIME; time.compareTo(END_TIME) <= 0; time = time.plusHours(1)) {
            y = (int) timeToPixel(time) + 15;
            g2.drawString(time.toString(), TIME_COL_WIDTH - (FONT_LETTER_PIXEL_WIDTH * time.toString().length()) - 5, y);
        }
    }

    private void drawEvents() {
        double x;
        double y0;

        for (CalendarEvent event : events) {
            if (!dateInRange(event.getDate())) continue;

            x = dayToPixel(event.getDate().getDayOfWeek());
            y0 = timeToPixel(event.getStart());

            Rectangle2D rect = new Rectangle2D.Double(x, y0, dayWidth, (timeToPixel(event.getEnd()) - timeToPixel(event.getStart())));
            Color origColor = g2.getColor();
            g2.setColor(event.getColor());
            g2.fill(rect);
            g2.setColor(origColor);

            // Draw time header

            // Store the current font state
            Font origFont = g2.getFont();

            final float fontSize = origFont.getSize() - 1.6F;

            // Create a new font with same properties but bold
            Font newFont = origFont.deriveFont(Font.BOLD, fontSize);
            g2.setFont(newFont);

            g2.drawString(event.getStart() + " - " + event.getEnd(), (int) x + 5, (int) y0 + 11);

            // Unbolden
            g2.setFont(origFont.deriveFont(fontSize));

            // Draw the event's text
            g2.drawString(event.getText(), (int) x + 5, (int) y0 + 23);

            // Reset font
            g2.setFont(origFont);
        }
    }

    protected double getDayWidth() {
        return dayWidth;
    }



    protected abstract void setRangeToToday();

    public void goToToday() {
        setRangeToToday();
        repaint();
    }

    public void addEvent(CalendarEvent event) {
        events.add(event);
        repaint();
    }

    public boolean removeEvent(CalendarEvent event) {
        boolean removed = events.remove(event);
        repaint();
        return removed;
    }

    public void setEvents(ArrayList<CalendarEvent> events) {
        this.events = events;
        repaint();
    }
}