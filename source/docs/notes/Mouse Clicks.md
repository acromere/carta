# Mouse Clicks

## Problem Statement

Mouse clicks are the primary input mechanism for the user in Cartesia. The user
is particularly sensitive to the mouse-down event and where the user perceives
this event occurs on the screen. Mouse clicks, the combination of a mouse-down
event and a mouse-up event, can be another useful input mechanism. However, they 
should be used with caution due to the delay in time and potential motion 
between the mouse-down and mouse-up events. This can lead to unintended actions 
or misinterpretation of user intent.

## Parameters

### Actions

Thankfully, there are only a few actions possible with a mouse other than moving
the cursor around, and they all have to do with the wheel or buttons. For any 
button there are five actions, down, up, click (down and up together), drag, and 
release (button up from dragging). The mouse wheel has only two actions, scroll
up and scroll down.

### Modifiers

There are four modifiers, and their combinations, that can be used with mouse
actions. The modifiers are ctrl, alt, shift, and meta. They can be used in any 
combination as well like ctrl-shift and alt-meta. These modifiers can be applied
to any of the mouse actions.

### Situations

There are also some situations that may affect how mouse actions are handled.
They are: "cursor over geometry", "geometry is selected". These situations need
to be taken into account when determining what command to execute or what a 
command should do.

## Considerations

It is desired that as many mouse actions that can be done immediately with mouse 
down be done. The reason is for user response. Assume the user needs to get work
accomplished quickly and the mouse down action is the first action the user can
make. Other applications make the user go through the mouse down/mouse up cycle
before making a choice; this slows the user down. Can we avoid that, or shift 
important actions to the mouse down action? Does shifting actions to the mouse
down action shadow the use of full-cycle options?

Something else to consider is the mouse cursor is often used to prompt the user 
for a specific type of input. We may not need to change the cursor, but we may 
need to change the mouse cursor to better match the user's intent. For example, 
if the user is trying to select a point, then the cursor should be a crosshair 
and the mouse-down event should be mapped to a point command. If the user is 
trying to select a shape, then the cursor should be the standard cursor and the 
mouse-down event should be mapped to the select command.

Something that Cartesia has not considered yet is "hover" actions. We have not
considered "hover" actions because they are possible with touch gestures. But 
this doesn't necessarily mean that we should not consider them. 

## Potential Configuration

### Default Mouse Actions
Here are the expected default actions and commands for Cartesia:
- Mouse-down, no modifier in an open area. Mouse-up ignored: Unselect all geometry
- Mouse-down, no modifier, over geometry. Mouse-up ignored: Select geometry
- Mouse-down, ctrl-drag, mouse-up: Add to selected geometry by window contains
- Mouse-down, shift-drag. Mouse-up ignored: Move the viewpoint
- Scroll-up, no modifier: Zoom in
- Scroll down, no modifier: Zoom out

### Draw.io Style Mouse Actions
A different configuration to consider to avoid focusing on the default configuration:
- Mouse-down, no modifier, mouse-up in an open area: Unselect all geometry (Note the command is triggered on the mouse-up event)
- Mouse-down, no modifier over geometry. Mouse-up ignored: Select geometry (Note the command is triggered on the mouse-up event
- Mouse-down, shift-drag, mouse-up: Add to selected geometry by window contains
- Mouse-down, ctrl-drag. Mouse-up ignored: Move the viewpoint
- Ctrl-scroll-up: Zoom in
- Ctrl-scroll down: Zoom out

It would be nice if this were simple mappings from mouse events and modifiers to
commands, but it is not. Here is a simple example of the challenge. If there is 
not a current command on the command stack, then the mouse-down event is 
mapped into a select command followed by coordinates. If, however, there is a 
command on the command stack, then the mouse-down might need to be mapped to a 
point command instead of a select command.

Another option is to always return a point command when the mouse-down event is 
triggered. The receiving command would then have to determine what to do with 
the point like use it to select geometry or as a point.
