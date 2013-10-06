package ca.nengo.ui.lib.world.piccolo.objects.icons;

import java.awt.Graphics2D;

public class MinimizeIcon extends WindowIconBase {
	public MinimizeIcon(int size) {
		super(size);
	}

	@Override
	protected void paintIcon(Graphics2D g2) {
		int yPosition = getSize() - PADDING - 1;
		g2.drawLine(PADDING, yPosition, getSize() - PADDING, yPosition);

	}
}