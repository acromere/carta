# Version 1.4 Readiness

## Command Review
Put all the commands in a table and check that all are operational

|     | Tested     | Status | Name                    | Key                      |
|-----|------------|:------:|-------------------------|--------------------------|
| a3  | 2026-09-05 |   ✓    | Arc 3-Point             | draw-arc-3               |
| aa  | 2026-09-05 |   ✓    | Arc                     | draw-arc-2               |
| bb  | 2026-09-05 | Failed | Box 2-Point             | draw-box-2               |
| bk  | 2026-09-05 |   ✓    | Split                   | split                    |
| c2  | 2026-09-05 |   ✓    | Circle by Diameter      | draw-circle-diameter-2   |
| c3  | 2026-09-05 |   ✓    | Circle 3-Point          | draw-circle-3            |
| cc  | 2026-09-05 |   ✓    | Circle                  | draw-circle-2            |
| cp  | 2026-09-05 | Failed | Copy                    | copy                     |
| cs  | Deprecated |        | Select Window Intersect | select-window-intersect  |
| ea  | 2026-09-05 | Failed | Ellipse Arc             | draw-ellipse-arc-5       |
| ee  | 2026-09-05 |   ✓    | Ellipse                 | draw-ellipse-3           |
| fl  | 2026-09-05 |   ✓    | Flip                    | flip                     |
| gg  | 2026-09-05 |   ✓    | Toggle Grid             | grid-toggle              |
| hh  |            |        | Path                    | draw-path                |
| ii  | 2026-09-05 |   ✓    | Shape Information       | shape-information        |
| jn  | 2026-09-05 |   ✓    | Meet                    | join                     |
| ll  | 2026-09-05 |   ✓    | Line                    | draw-line-2              |
| lp  | 2026-09-05 |   ✓    | Perpendicular Line      | draw-line-perpendicular  |
| ma  | 2026-09-05 |   ✓    | Measure Angle           | measure-angle            |
| md  | 2026-09-05 |   ✓    | Measure Distance        | measure-distance         |
| me  |            |        | Move Points             | move-points              |
| mi  | 2026-09-05 | Failed | Mirror                  | mirror                   |
| ml  | 2026-09-05 |   ✓    | Measure Path            | measure-length           |
| mm  | 2026-09-05 |   ✓    | Marker                  | draw-marker              |
| mp  | 2026-09-05 |   ✓    | Measure Point           | measure-point            |
| mv  | 2026-09-05 |   ✓    | Move                    | move                     |
| rr  |            |        | Toggle Reference Points | reference-toggle         |
| vt  | 2026-09-05 |   ✓    | Set View Top            | camera-view-top          |
| oo  | 2026-09-05 |   ✓    | Undo                    | undo                     |
| pa  |            |        | Camera Pan              | camera-move              |
| rc  | 2026-09-05 | Failed | Radial Copy             | radial-copy              |
| rm  | 2026-09-05 |   ✓    | Trim                    | trim                     |
| ro  | 2026-09-05 |   ✓    | Rotate                  | rotate                   |
| sg  | 2026-09-05 |   ✓    | Toggle Grid Snap        | snap-grid-toggle         |
| si  | 2026-09-05 |   ✓    | Snap Intersection       | snap-intersection        |
| sm  | 2026-09-05 |   ✓    | Snap Midpoint           | snap-midpoint            |
| sn  | 2026-09-05 |   ✓    | Snap Center             | snap-center              |
| sp  | 2026-09-05 | Failed | Snap Nearest            | snap-nearest             |
| st  | 2026-09-05 | Failed | Stretch                 | stretch                  |
| sz  | 2026-09-05 | Failed | Scale                   | scale                    |
| tt  | 2026-09-05 |   ✓    | Text                    | draw-text                |
| uu  | 2026-09-05 |   ✓    | Redo                    | redo                     |
| vl  | 2026-09-05 |   ✓    | Set View Left           | camera-view-left         |
| vp  | 2026-09-05 |   ✓    | View Point              | camera-view-point        |
| vr  | 2026-09-05 |   ✓    | Set View Right          | camera-view-right        |
| vsl | 2026-09-05 |   ✓    | Rotate View Left        | camera-view-rotate-left  |
| vsr | 2026-09-05 |   ✓    | Rotate View Right       | camera-view-rotate-right |
| vv  | 2026-09-05 | Failed | Curve                   | draw-curve-4             |
| ws  | Deprecated |        | Select Window Contain   | select-window-contain    |
| wu  |            |        | Update View             | view-update              |
| wx  |            |        | Delete View             | view-delete              |
| xt  | 2026-09-05 |   ✓    | Extend                  | extend                   |
| xx  | 2026-09-05 |   ✓    | Delete                  | delete                   |
| yc  | 2026-09-05 |   ✓    | Layer Create            | layer-create             |
| yd  | 2026-09-05 |   ✓    | Layer Show              | layer-show               |
| yh  | 2026-09-05 |   ✓    | Layer Hide              | layer-hide               |
| yk  | 2026-09-05 |   ✓    | Layer Current           | layer-current            |
| ys  | 2026-09-05 | Failed | Layer Sublayer          | layer-sublayer           |
| yx  | 2026-09-05 |   ✓    | Layer Delete            | layer-delete             |
| yy  | 2026-09-05 |   ✓    | Layer Toggle            | layer-toggle             |
| za  | 2026-09-05 |   ✓    | Zoom All                | camera-zoom-all          |
| zi  | 2026-09-05 |   ✓    | Zoom In                 | camera-zoom-in           |
| zm  | 2026-09-05 |   ✓    | Zoom                    | camera-zoom              |
| zo  | 2026-09-05 |   ✓    | Zoom Out                | camera-zoom-out          |
| zp  | 2026-09-05 |   ✓    | Zoom Previous           | camera-view-previous     |
| zs  | 2026-09-05 |   ✓    | Zoom Selected           | camera-zoom-selected     |
| zw  | 2026-09-05 | Failed | Zoom Window             | camera-zoom-window       |
| ww  |            |        | Create View             | view-create              |
