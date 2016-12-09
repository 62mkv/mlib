/**
 * Override the default behavior of __doMoveLegend which was constraining the legend
 * to the document's body rather than to the element the chart was rendered to.
 * 
 * This is a fix for RPWEB-2012
 * 
 * @param {Object} e The mouse move event object
 */EJSC.Chart.prototype.__doMoveLegend = function(e) {
  if (this.__legend_is_moving) {
    var cont = this.__el_container;
    if (!e) var e = window.event;
    var xy = EJSC.utility.__realXY(e);
    var ol = EJSC.utility.__documentOffsetLeft(cont, true);
    var ot = EJSC.utility.__documentOffsetTop(cont, true);
    // If the body's width is bigger than the container, use the container to constrain
    var mw = (document.body.offsetWidth > (ol + cont.offsetWidth)) ? cont.offsetWidth + ol : document.body.offsetWidth;
    // If the body's height is bigger than the container, use the container to constrain
    var mh = (document.body.offsetHeight > (ot + cont.offsetHeight)) ? cont.offsetHeight + ot : document.body.offsetHeight;
    var tmp_left = xy.x - this.__legend_off_x;
    // If we are moving past the boundaries of the container's min width, constrain to the container
    if (tmp_left < 0) {
      tmp_left = 0;
    }
    if (tmp_left > (mw - ol - this.__el_legend.offsetWidth)) {
      tmp_left = (mw - ol - this.__el_legend.offsetWidth)
    }
    var tmp_top = xy.y - this.__legend_off_y;
    // If we are moving past the boundaries of the container's min height, constrain to the container
    if (tmp_top < 0) {
      tmp_top = 0;
    }
    if (tmp_top > (mh - ot - this.__el_legend.offsetHeight)) {
      tmp_top = (mh - ot - this.__el_legend.offsetHeight)
    }
    this.__el_legend.style.left = tmp_left + "px";
    this.__el_legend.style.top = tmp_top + "px"
  }
};