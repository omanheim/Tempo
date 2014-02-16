/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pedometer;

/**
 * Calculates and displays the distance walked.
 * 
 * @author Levente Bagi
 */
public class DistanceUpdater implements StepListener {

	private Listener mListener;
	float mDistance;
	PedometerSettings mSettings;
	Utils mUtils;
	float mStepLength;

	public interface Listener {
		public void distanceChanged(float value);
		public void passValue();
	}

	public DistanceUpdater(Listener listener, PedometerSettings settings,
			Utils utils) {
		mDistance = 0;
		mListener = listener;
		mUtils = utils;
		mSettings = settings;
		reloadSettings();
	}

	public void setDistance(float distance) {
		mDistance = distance;
		notifyListener();
	}

	public void reloadSettings() {
		mStepLength = mSettings.getStepLength();
		notifyListener();
	}

	public void onStep() {
		mDistance += (float) (mStepLength / 63360.0); // inches/mile
		notifyListener();
	}

	private void notifyListener() {
		mListener.distanceChanged(mDistance);
	}

	public void passValue() {
		// Callback of StepListener - Not implemented
	}


}