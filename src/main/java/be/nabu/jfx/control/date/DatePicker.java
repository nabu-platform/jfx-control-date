package be.nabu.jfx.control.date;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Not using date or calendar because they are not immutable
 * 
 * Known bugs
 * ----------
 * 1) When you click on the textfield the context menu pops up, however it appears that
 * once the context menu is present, a mouse click on the text field no longer correctly updates the caret position
 * this means as long as the context menu is visible, you can NOT properly select fields with the mouse (linux only)
 * 
 * 2) When you display the day of the month and the day of the week at the same time, you can NOT update the day of the month
 * by entering the numbers manually. This is because each change is validated by parsing the resulting value
 * however while typing the day of month field, chances are the day of week does NOT match the day of month
 * Solution is to either not display them at the same time, edit them using the popup or use the arrow keys
 */
public class DatePicker extends TextField {
	
	private ObjectProperty<Long> timestamp = new SimpleObjectProperty<Long>(this, "timestamp");
	private ObjectProperty<Locale> locale = new SimpleObjectProperty<Locale>(this, "locale");
	private ObjectProperty<TimeZone> timezone = new SimpleObjectProperty<TimeZone>(this, "timezone");
	private ObjectProperty<String> format = new SimpleObjectProperty<String>(this, "format");
	private ObjectProperty<DateFilter> filter = new SimpleObjectProperty<DateFilter>(this, "filter");
	
	private List<String> fields = null;
	
	private DateFormat formatter;
	
	private Range<Integer> selectedRange;
	
	private ContextMenu contextMenu;
	
	private PopupCalendar popupCalendar;
	
	private boolean hideTimeControls = false;
	
	private boolean setting = false;
	
	/**
	 * The key handler must be registered on both the textfield and the popup to work properly for up and down keys
	 */
	private EventHandler<KeyEvent> keyHandler;
	
	public DatePicker() {
		this(null);
	}
	public DatePicker(String format) {
		this(format, null);
	}
	public DatePicker(String format, TimeZone timezone) {
		this(format, timezone, null);
	}
	public DatePicker(String format, TimeZone timezone, Locale locale) {
		// initializes all the listeners
		initialize();
		
		// set default values, the order is important for the listeners
		this.timezone.setValue(timezone == null ? TimeZone.getDefault() : timezone);
		this.locale.setValue(locale == null ? Locale.getDefault() : locale);
		this.format.setValue(format == null ? "yyyy/MM/dd HH:mm:ss" : format);
//		timestamp.setValue(new Date().getTime());
		
		// find and select a field
		refreshRange();
		selectRange();
	}

	private void selectRange(Range<Integer> range) {
		if (range != null) {
			selectRange(range.getStart(), range.getEnd() + 1);
		}
	}
	
	private void selectRange() {
		refreshRange();
		selectRange(selectedRange);
	}
	
	public ObjectProperty<DateFilter> filterProperty() {
		return filter;
	}
	
	private void updateTimestamp() {
		if (getText() != null && !getText().trim().isEmpty()) {
			ParsePosition position = new ParsePosition(0);
			Date date = formatter.parse(getText(), position);
			if (position.getErrorIndex() < 0) {
				// the change event is not triggered if the value hasn't changed
				// so if you fill in the same twice, the textual value might not be updated to the proper format but it must
				// e.g. the first time you fill in "1" for month, because it is different from the current month, the timestamp is updated and the textfield as well, resulting in 01
				// if you type in 1 again, there is no change but the text field must still become 01
				if (timestamp.getValue() != null && timestamp.getValue().equals(date.getTime()))
					setText(formatter.format(date));
				else
					timestamp.setValue(date.getTime());
			}
		}
	}
	
	private void updateText() {
		updateText(timestamp.getValue());
	}
	
	private void updateText(Long value) {
		if (formatter != null && value != null) {
			setting = true;
			try {
				setText(formatter.format(new Date(value)));
				refreshRange();
				selectRange();
			}
			finally {
				setting = false;
			}
		}
		else if (value == null) {
			setText("");
		}
	}
	
	private void refreshRange() {
		if (selectedRange == null) {
			// check if we have a range at the start
			selectedRange = getValueRange(0);
			// if not, check for the next range
			if (selectedRange == null)
				findNextRange(0);
		}
		else
			selectedRange = getValueRange(selectedRange.getStart());
	}

	private void initialize() {
		// if the text property is updated (through api or by the user) this should be processed
		// note that we do NOT update the timestamp just yet, the user may have typed "1" because he wants to type "12"
		// however if you would update the timestamp you would send out an "invalid" update event to whoever is listening
		// and the updated textbox might now contain "01" which would be awkward for the user
		textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!setting) {
					if (oldValue == null || oldValue.trim().isEmpty()) {
						timestamp.set(new Date().getTime());
					}
					else if (newValue != null && !newValue.trim().isEmpty()) {
						ParsePosition position = new ParsePosition(0);
						Date parsed = getFormatter().parse(newValue, position);
						// not a valid date according to the format, revert value
						if (position.getErrorIndex() >= 0) 
							setText(oldValue);
						else if (filter.isNotNull().getValue() && !filter.getValue().accept(parsed)) {
							// only revert to the old value if that was actually accepted
							// this protects against recursion, it is up to the developer to make sure the initial value (whatever it is) conforms to the filter
							if (filter.getValue().accept(getDate()))
								setText(oldValue);
						}
						refreshRange();
						// position the caret after what you just edited, that way you can continue typing
						positionCaret(selectedRange.getEnd() + 1);
					}
				}
			}
		});
		
		// hide the context menu and update the value if we lose focus
		focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean isFocused) {
				if (!isFocused) {
					// we need to update the timestamp because the user might have finished typing something
					updateTimestamp();
					getContextMenu().hide();
				}
				// otherwise, if we gain focus, we must make sure we have something selected
				else {
					// first thing we type in an empty field
					if (timestamp.getValue() == null) {
						timestamp.set(new Date().getTime());
					}
					selectRange();
				}
			}
		});

		// if you click a mouse somewhere, select the field nearby
		addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// select the new range
				selectedRange = getValueRange(getCaretPosition());
				// if there is no range where the user clicked, get the closest one
				if (selectedRange == null) {
					selectedRange = findPreviousRange(getCaretPosition());
					if (selectedRange == null)
						selectedRange = findNextRange(getCaretPosition());
				}
				if (selectedRange != null) {
					// select the range we just found
					selectRange();
					event.consume();
					getContextMenu().show(DatePicker.this, Side.BOTTOM, 0, 0);
					requestFocus();
				}
			}
		});
		
		keyHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				// if you are going left or right, and you go past the boundaries, select the whole previous or next bit
				if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT) {
					// first off, update the timestamp because the user is done editing the previous field
					updateTimestamp();
					// we need a new range based on the current range
					Range<Integer> sibling = event.getCode() == KeyCode.LEFT 
						? findPreviousRange(selectedRange.getStart())
						: findNextRange(selectedRange.getStart());
					// only update the selected range if there is in fact another range
					if (sibling != null)
						selectedRange = sibling;
					selectRange(selectedRange);
					// stop default behavior
					event.consume();
				}
				// increase/decrease the current field
				else if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
					Calendar calendar = getCalendar();
					int factor = event.getCode() == KeyCode.UP ? 1 : -1;
					int calendarField = fieldToCalendarField(getFieldIndex(selectedRange.getStart()));
					calendar.roll(calendarField, factor);
					if (filter.getValue() == null || filter.getValue().accept(calendar.getTime()))
						setCalendar(calendar);
					refreshRange();
					selectRange();
					event.consume();
				}
				// indicate that you are done editing the current field
				else if (event.getCode() == KeyCode.ENTER) {
					updateTimestamp();
					refreshRange();
					selectRange();
					event.consume();
				}
				else if (event.getCode() == KeyCode.DELETE) {
					timestamp.setValue(null);
					event.consume();
				}
			}
		};
		
		// the arrow keys are redefined and should not exhibit their normal caret-repositioning behavior
		// left and right move between groups whilst up and down increase or decrease the value of the selected field
		addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
		
		addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				// typing letters will move the caret
				// this is usually ok unless you type something wrong, the value will be reset but the caret will still move
				if (selectedRange != null && event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN && event.getCode() != KeyCode.LEFT && event.getCode() != KeyCode.RIGHT && event.getCode() != KeyCode.ENTER)
					positionCaret(selectedRange.getEnd() + 1);
			}
		});
		
		format.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Pattern pattern = Pattern.compile("[\\w]+");
				Matcher matcher = pattern.matcher(newValue);
				fields = new ArrayList<String>();
				while (matcher.find()) {
					String group = matcher.group();
					if (group.matches(".*[GWwFzZ]+.*"))
						throw new IllegalArgumentException("The formatted string " + group + " is not supported");
					else
						fields.add(group);
				}
				updateFormatter();
				updateText();
				// update the context menu, it relies on the format
				buildContextMenu();
			}
		});
		
		timezone.addListener(new ChangeListener<TimeZone>() {
			@Override
			public void changed(ObservableValue<? extends TimeZone> arg0, TimeZone arg1, TimeZone newValue) {
				if (formatter != null) {
					formatter.setTimeZone(newValue);
					updateText();
				}
			}
		});
		
		locale.addListener(new ChangeListener<Locale>() {
			@Override
			public void changed(ObservableValue<? extends Locale> arg0, Locale arg1, Locale newValue) {
				updateFormatter();
				updateText();
			}
		});
		
		timestamp.addListener(new ChangeListener<Long>() {
			@Override
			public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
				if (arg2 == null) {
					setText("");
				}
				else {
					updateText(arg2);
				}
			}
		});		
		
		getStyleClass().add("nabu-date-picker");
	}
	
	private void updateFormatter() {
		if (format.getValue() != null) {
			formatter = locale.getValue() == null ? new SimpleDateFormat(format.getValue()) : new SimpleDateFormat(format.getValue(), locale.getValue());
			formatter.setLenient(false);
			if (timezone != null)
				formatter.setTimeZone(timezone.getValue());
		}
	}
	
	private void buildContextMenu() {
		if (contextMenu == null) {
			CustomMenuItem menuItem = new CustomMenuItem();
			menuItem.setHideOnClick(false);
			menuItem.getStyleClass().add("nabu-date-picker-popup");
			contextMenu = new ContextMenu();
			contextMenu.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
			contextMenu.getStyleClass().add("nabu-date-picker-popup");
			// this prevents context menu from closing when you click on the text field (allowing you for example to select parts)
			contextMenu.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					event.consume();
				}
			});
			// this prevents the context menu from gaining focus when you move over the text field
			contextMenu.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					menuItem.getContent().requestFocus();
					event.consume();
				}
			});
			contextMenu.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (arg2 != null && arg2) {
						menuItem.getContent().requestFocus();
					}
				}
			});
			contextMenu.getItems().add(menuItem);
			setContextMenu(contextMenu);
		}
		final Parent popup = getPopupCalendar().build();
		((CustomMenuItem) contextMenu.getItems().get(0)).setContent(popup);
		popup.requestFocus();
	}
	
	private PopupCalendar getPopupCalendar() {
		if (popupCalendar == null)
			popupCalendar = new PopupCalendar(this);
		return popupCalendar;
	}
	
	/**
	 * Finds the next part
	 * @param position
	 * @return
	 */
	private Range<Integer> findNextRange(int position) {
		Range<Integer> range = getValueRange(position);
		for (int i = range == null ? position : range.getEnd() + 1; i < getText().length(); i++) {
			if (isValidValueChar(getText().charAt(i)))
				return getValueRange(i);
		}
		return null;
	}
	private Range<Integer> findPreviousRange(int position) {
		Range<Integer> range = getValueRange(position);
		for (int i = range == null ? position : range.getStart() - 1; i >= 0; i--) {
			if (i < getText().length() && isValidValueChar(getText().charAt(i)))
				return getValueRange(i);
		}
		return null;
	}
	
	private Range<Integer> getValueRange(int position) {
		if (position >= getText().length())
			position = getText().length() - 1;
		
		if (position >= 0 && isValidValueChar(getText().charAt(position))) {
			// find start
			Integer start = position;
			for (int i = position; i >= 0; i--) {
				if (isValidValueChar(getText().charAt(i)))
					start = i;
				else
					break;
			}
			// find end
			Integer end = position;
			for (int i = position; i < getText().length(); i++) {
				if (isValidValueChar(getText().charAt(i)))
					end = i;
				else
					break;
			}
			return new Range<Integer>(start, end);
		}
		else
			return null;
	}
	
	/**
	 * Follows the regex \w convention
	 * @param charCode
	 * @return
	 */
	private static boolean isValidValueChar(int charCode) {
		return (charCode >= 48 && charCode <= 57);
//			|| (charCode >= 65 && charCode <= 90)
//			|| (charCode >= 97 && charCode <= 122)
//			|| charCode == 95;
	}
	
	private Integer getFieldIndex(int startPosition) {
		if (startPosition >= getText().length())
			startPosition = getText().length() - 1;
		
		if (!isValidValueChar(getText().charAt(startPosition)))
			return null;
		
		int part = 0;
		boolean isAlfaNumeric = true;
		for (int i = startPosition; i >= 0; i--) {
			if (isValidValueChar(getText().charAt(i))) {
				if (!isAlfaNumeric) {
					part++;
					isAlfaNumeric = true;
				}
			}
			else {
				if (isAlfaNumeric)
					isAlfaNumeric = false;
			}
		}
		return part;
	}
	
	@Override
	public String getUserAgentStylesheet() {
		return DatePicker.class.getClassLoader().getResource("jfx-date-picker.css").toExternalForm();
	}
	
	public ObjectProperty<Long> timestampProperty() {
		return timestamp;
	}
	public ObjectProperty<Locale> localeProperty() {
		return locale;
	}
	public ObjectProperty<TimeZone> timezoneProperty() {
		return timezone;
	}
	public ObjectProperty<String> formatProperty() {
		return format;
	}

	public boolean getHideTimeControls() {
		return hideTimeControls;
	}
	public void setHideTimeControls(boolean hideTimeControls) {
		this.hideTimeControls = hideTimeControls;
	}
	public Date getDate() {
		return timestamp.getValue() == null ? null : new Date(timestamp.getValue());
	}
	
	public void setDate(Date date) {
		timestamp.setValue(date == null ? null : date.getTime());
	}
	
	public Calendar getCalendar() {
		if (timestamp.getValue() == null)
			return null;
		else {
			Calendar calendar = Calendar.getInstance(timezone.getValue(), locale.getValue());
			calendar.setTime(new Date(timestamp.getValue()));
			return calendar;
		}
	}
	
	public void setCalendar(Calendar calendar) {
		timezone.setValue(calendar.getTimeZone());
		timestamp.setValue(calendar.getTime().getTime());
	}
	
	public DateFormat getFormatter() {
		return formatter;
	}
	
	int fieldToCalendarField(int field) {
		if (field >= fields.size() || field < 0)
			return -1;
		
		String currentField = fields.get(field);
		if (currentField.matches("[m]+"))
			return Calendar.MINUTE;
		else if (currentField.matches("[s]+"))
			return Calendar.SECOND;
		else if (currentField.matches("[HkKh]+"))
			return Calendar.HOUR_OF_DAY;
		else if (currentField.matches("[D]+"))
			return Calendar.DATE;
		else if (currentField.matches("[d]+"))
			return Calendar.DAY_OF_MONTH;
		else if (currentField.matches("[F]+"))
			return Calendar.DAY_OF_WEEK_IN_MONTH;
		else if (currentField.matches("[E]+"))
			return Calendar.DAY_OF_WEEK;
		else if (currentField.matches("[M]+"))
			return Calendar.MONTH;
		else if (currentField.matches("[y]+"))
			return Calendar.YEAR;
		else if (currentField.matches("[S]+"))
			return Calendar.MILLISECOND;
		else if (currentField.matches("[w]+"))
			return Calendar.WEEK_OF_YEAR;
		else if (currentField.matches("[W]+"))
			return Calendar.WEEK_OF_MONTH;
		else if (currentField.matches("[a]+"))
			return Calendar.AM_PM;
		else
			return -1;
	}
	
	void incrementCalendarField(int calendarField, int amount, boolean roll) {
		if (calendarField >= 0) {
			Calendar calendar = getCalendar();
			if (roll)
				calendar.roll(calendarField, amount);
			else
				calendar.add(calendarField, amount);
			setCalendar(calendar);
		}
	}
	
	int getFieldIndex(String characters) {
		for (int i = 0; i < fields.size(); i++) {
			if (fields.get(i).matches("[" + characters + "]+"))
				return i;
		}
		return -1;
	}
}
