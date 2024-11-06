/*
* Copyright (C) 2013 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.jfx.control.date;

import java.util.Calendar;
import java.util.Date;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class PopupCalendar {
	
	private DatePicker datePicker;
	
	private GridPane dayGrid;
	
	private Label lblMonth, lblYear;
	
	private Slider sldHour, sldMinute, sldSecond, sldMillisecond;
	
	private Button btnPreviousYear, btnNextYear, btnPreviousMonth, btnNextMonth;
	
	private ChangeListener<Long> monthListener = new ChangeListener<Long>() {
		@Override
		public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
			buildMonthLabel();
		}
	};
	
	private ChangeListener<Long> yearListener = new ChangeListener<Long>() {
		@Override
		public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
			buildYearLabel();
		}
	};
	
	private ChangeListener<Long> dayListener = new ChangeListener<Long>() {
		@Override
		public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
			buildDayGrid();
		}
	};
	
	private ChangeListener<Long> hourListener = new ChangeListener<Long>() {
		@Override
		public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
			buildHourSlider();
		}
	};
	
	private ChangeListener<Long> minuteListener = new ChangeListener<Long>() {
		@Override
		public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
			buildMinuteSlider();
		}
	};
	private ChangeListener<Long> secondListener = new ChangeListener<Long>() {
		@Override
		public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
			buildSecondSlider();
		}
	};
	private ChangeListener<Long> millisecondListener = new ChangeListener<Long>() {
		@Override
		public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
			buildMillisecondSlider();
		}
	};

	public PopupCalendar(DatePicker datePicker) {
		this.datePicker = datePicker;
		datePicker.filterProperty().addListener(new ChangeListener<DateFilter>() {
			@Override
			public void changed(ObservableValue<? extends DateFilter> arg0, DateFilter arg1, DateFilter arg2) {
				updateFilters();
			}
		});
		datePicker.timestampProperty().addListener(new ChangeListener<Long>() {
			@Override
			public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
				updateFilters();
			}
		});
	}
	
	private void destroy() {
		datePicker.timestampProperty().removeListener(yearListener);
		datePicker.timestampProperty().removeListener(monthListener);
		datePicker.timestampProperty().removeListener(dayListener);
		datePicker.timestampProperty().removeListener(minuteListener);
		datePicker.timestampProperty().removeListener(secondListener);
		datePicker.timestampProperty().removeListener(millisecondListener);
	}
	
	public Parent build() {
		// destroy previous
		destroy();
		
		VBox vbxMain = new VBox();
		vbxMain.getStyleClass().add("nabu-date-picker-calendar");
		
		GridPane dateGrid = new GridPane();

		ColumnConstraints constraints1 = new ColumnConstraints();
		constraints1.setPercentWidth(20);
		ColumnConstraints constraints2 = new ColumnConstraints();
		constraints2.setPercentWidth(60);
		ColumnConstraints constraints3 = new ColumnConstraints();
		constraints3.setPercentWidth(20);
		dateGrid.getColumnConstraints().addAll(
			constraints1,
			constraints2,
			constraints3
	    );
		
		dateGrid.setVgap(10);

		int rowIndex = 0;
		
		final int yearField = datePicker.getFieldIndex("y");
		// if we have years, show them
		if (yearField >= 0) {
			btnPreviousYear = new Button("<");
			btnPreviousYear.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {
					datePicker.incrementCalendarField(datePicker.fieldToCalendarField(yearField), -1, false);
				}
			});
			lblYear = new Label();
			datePicker.timestampProperty().addListener(yearListener);
			lblYear.getStyleClass().add("nabu-date-picker-year");
			buildYearLabel();
			btnNextYear = new Button(">");
			btnNextYear.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {
					datePicker.incrementCalendarField(datePicker.fieldToCalendarField(yearField), 1, false);
				}
			});
			GridPane.setHalignment(btnPreviousYear, HPos.CENTER);
			GridPane.setHalignment(lblYear, HPos.CENTER);
			GridPane.setHalignment(btnNextYear, HPos.CENTER);
			dateGrid.addRow(rowIndex++, btnPreviousYear, lblYear, btnNextYear);
		}
		
		final int monthField = datePicker.getFieldIndex("M");
		// if we have months, show them as well
		if (monthField >= 0) {
			btnPreviousMonth = new Button("<");
			btnPreviousMonth.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {
					datePicker.incrementCalendarField(datePicker.fieldToCalendarField(monthField), -1, false);
				}
			});
			lblMonth = new Label();
			datePicker.timestampProperty().addListener(monthListener);
			lblMonth.getStyleClass().add("nabu-date-picker-month");
			buildMonthLabel();
			btnNextMonth = new Button(">");
			btnNextMonth.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {
					datePicker.incrementCalendarField(datePicker.fieldToCalendarField(monthField), 1, false);
				}
			});
			GridPane.setHalignment(btnPreviousMonth, HPos.CENTER);
			GridPane.setHalignment(lblMonth, HPos.CENTER);
			GridPane.setHalignment(btnNextMonth, HPos.CENTER);
			dateGrid.addRow(rowIndex++, btnPreviousMonth, lblMonth, btnNextMonth);
		}
		
		if (dateGrid.getChildren().size() > 0)
			vbxMain.getChildren().add(dateGrid);
		
		final int dayField = datePicker.getFieldIndex("dDFE");
		if (dayField >= 0) {
			dayGrid = new GridPane();
			dayGrid.getStyleClass().add("nabu-date-picker-day");
			buildDayGrid();
			// rebuild on change
			datePicker.timestampProperty().addListener(dayListener);
			vbxMain.getChildren().add(dayGrid);
		}

		if (!datePicker.getHideTimeControls()) {
			GridPane timeGrid = new GridPane();
			
			ColumnConstraints c1 = new ColumnConstraints();
			c1.setPercentWidth(35);
			ColumnConstraints c2 = new ColumnConstraints();
			c2.setPercentWidth(65);
			timeGrid.getColumnConstraints().addAll(
				c1,
				c2
		    );
			
			timeGrid.setVgap(10);
	
			rowIndex = 0;
	
			final int hourField = datePicker.getFieldIndex("HkKh");
			if (hourField >= 0) {
				sldHour = new Slider(0, 23, 0);
				buildHourSlider();
				sldHour.valueProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
						Calendar calendar = datePicker.getCalendar();
						calendar.set(Calendar.HOUR_OF_DAY, arg2.intValue());
						datePicker.setCalendar(calendar);					
					}
				});
				datePicker.timestampProperty().addListener(hourListener);
				timeGrid.addRow(rowIndex++, new Label("Hours:"), sldHour);
			}
	
			final int minuteField = datePicker.getFieldIndex("m");
			if (minuteField >= 0) {
				sldMinute = new Slider(0, 59, 0);
				buildMinuteSlider();
				sldMinute.valueProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
						Calendar calendar = datePicker.getCalendar();
						calendar.set(Calendar.MINUTE, arg2.intValue());
						datePicker.setCalendar(calendar);					
					}
				});
				datePicker.timestampProperty().addListener(minuteListener);
				timeGrid.addRow(rowIndex++, new Label("Minutes:"), sldMinute);
			}
			
			final int secondField = datePicker.getFieldIndex("s");
			if (secondField >= 0) {
				sldSecond = new Slider(0, 59, 0);
				buildSecondSlider();
				sldSecond.valueProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
						Calendar calendar = datePicker.getCalendar();
						calendar.set(Calendar.SECOND, arg2.intValue());
						datePicker.setCalendar(calendar);					
					}
				});
				datePicker.timestampProperty().addListener(secondListener);
				timeGrid.addRow(rowIndex++, new Label("Seconds:"), sldSecond);
			}
	
			final int millisecondField = datePicker.getFieldIndex("S");
			if (millisecondField >= 0) {
				sldMillisecond = new Slider(0, 999, 0);
				buildMillisecondSlider();
				sldMillisecond.valueProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
						Calendar calendar = datePicker.getCalendar();
						calendar.set(Calendar.MILLISECOND, arg2.intValue());
						datePicker.setCalendar(calendar);					
					}
				});
				datePicker.timestampProperty().addListener(millisecondListener);
				timeGrid.addRow(rowIndex++, new Label("Milliseconds:"), sldMillisecond);
			}
	
			if (timeGrid.getChildren().size() > 0)
				vbxMain.getChildren().add(timeGrid);
		}
		
		updateFilters();
		
		return vbxMain;
	}
	
	private void buildHourSlider() {
		Calendar calendar = datePicker.getCalendar();
		if (calendar != null)
			sldHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
	}
	private void buildMinuteSlider() {
		Calendar calendar = datePicker.getCalendar();
		if (calendar != null)
			sldMinute.setValue(calendar.get(Calendar.MINUTE));
	}
	private void buildSecondSlider() {
		Calendar calendar = datePicker.getCalendar();
		if (calendar != null)
			sldSecond.setValue(calendar.get(Calendar.SECOND));
	}
	private void buildMillisecondSlider() {
		Calendar calendar = datePicker.getCalendar();
		if (calendar != null)
			sldMillisecond.setValue(calendar.get(Calendar.MILLISECOND));
	}
	
	private void buildYearLabel() {
		Calendar calendar = datePicker.getCalendar();
		if (calendar != null)
			lblYear.setText("" + calendar.get(datePicker.fieldToCalendarField(datePicker.getFieldIndex("y"))));
	}
	
	private void updateFilters() {
		if (datePicker.getCalendar() != null) {
			DateFilter filter = datePicker.filterProperty().isNotNull().getValue() ? datePicker.filterProperty().getValue() : new AcceptAllFilter();
			// check years
			Calendar calendar = datePicker.getCalendar();
			calendar.add(Calendar.YEAR, -1);
			if (btnPreviousYear != null)
				btnPreviousYear.disableProperty().set(!filter.accept(calendar.getTime()));
			calendar.add(Calendar.YEAR, 2);
			if (btnNextYear != null)
				btnNextYear.disableProperty().set(!filter.accept(calendar.getTime()));
			// check months
			calendar = datePicker.getCalendar();
			calendar.add(Calendar.MONTH, -1);
			if (btnPreviousMonth != null)
				btnPreviousMonth.disableProperty().set(!filter.accept(calendar.getTime()));
			calendar.add(Calendar.MONTH, 2);
			if (btnNextMonth != null)
				btnNextMonth.disableProperty().set(!filter.accept(calendar.getTime()));
		}
	}
	
	private void buildMonthLabel() {
		Calendar calendar = datePicker.getCalendar();
		if (calendar != null)
			lblMonth.setText(calendar.getDisplayName(datePicker.fieldToCalendarField(datePicker.getFieldIndex("M")), Calendar.LONG, datePicker.localeProperty().getValue()));			
	}
	
	private void buildDayGrid() {
		dayGrid.getChildren().clear();
		
		Calendar calendar = datePicker.getCalendar();
		if (calendar != null) {
			// for each day, create an entry in the grid
			// the entries for the titles may be created multiple times but this is okay
			Calendar copy = (Calendar) calendar.clone();
			for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
				copy.set(Calendar.DAY_OF_MONTH, i);
				// get the column it belongs to
				int column = copy.get(Calendar.DAY_OF_WEEK);
				if (calendar.getFirstDayOfWeek() > 1)
					column -= (calendar.getFirstDayOfWeek() - 1);
				if (column <= 0)
					column = 8 - column;
				// so far it was 1-based, grid is 0-based
				column--;
				// 1-based again but this time it's ok because the first row is taken up by display names
				int row = copy.get(Calendar.WEEK_OF_MONTH);
				// set the displayname
				dayGrid.add(new Label(copy.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, datePicker.localeProperty().getValue())), column, 0);
				// set the actual day
				Button btnDay = new Button((copy.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + copy.get(Calendar.DAY_OF_MONTH));
				btnDay.disableProperty().setValue(datePicker.filterProperty().isNotNull().getValue() && !datePicker.filterProperty().getValue().accept(copy.getTime()));
				if (copy.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
					btnDay.getStyleClass().add("nabu-date-picker-day-selected");
				btnDay.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						Button btnThis = (Button) event.getSource();
						int day = new Integer(btnThis.getText());
						Calendar calendar = datePicker.getCalendar();
						calendar.set(Calendar.DAY_OF_MONTH, day);
						datePicker.setCalendar(calendar);
					}
				});
				dayGrid.add(btnDay, column, row);
			}
		}
	}
	
	private static class AcceptAllFilter implements DateFilter {
		@Override
		public boolean accept(Date date) {
			return true;
		}
	}
}
