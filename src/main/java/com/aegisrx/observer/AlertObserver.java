package com.aegisrx.observer;

import com.aegisrx.domain.DietaryAlert;

// handles events
public interface AlertObserver {
	void onAlert(DietaryAlert alert);
}
