

Usage
Basic

The generated code can be used to draw directly to a Canvas

Render_09055.draw(canvas, width, height);

This will draw the Svg inside rectangular bounds with an upper left corner at (0, 0) and a lower right at (width, height).

To translate the bounds to a custom anchor point (dx, dy): use the longer version of the draw method

Render_09055.draw(canvas, width, height, dx, dy);

Drawable

If you have chosen to include the Drawable code and want to use it inside e.g. an ImageView you can do so by first creating the Drawable:

Drawable icon = Render_09055.getDrawable(size);
yourImageView.setImageDrawable(icon);

Where size is the greatest dimension (width/height) that the generated drawable will have.
Icon Tinting

If you want to tint your icon with a certain color you can set the icon tint using the following method. Please be warned that all methods are static which means the tint is applied accross your app and not only for the current drawing operation. Therefor calling the clearColorTint method after a draw operation is highly suggested.

Render_09055.setColorTint(color);
Render_09055.clearColorTint();

If you don't want to worry about setting and clearing the tint value with every draw call the generated Drawables provide a "it just works" solution:

Drawable blueTintedIcon = Render_09055.getTintedDrawable(
                                size, Color.BLUE);

The icon will be tinted blue when it is generated and won't change after that so you don't have to worry about anything.

