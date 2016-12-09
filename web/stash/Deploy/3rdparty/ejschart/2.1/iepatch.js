// This is to allow IE to render correctly if the EJSC library is loaded
// dynamically, i.e., after the page is loaded.


if (typeof Ext !== "undefined" && Ext.isReady && Ext.isIE && document.namespaces) {
var patchFn = function() {
  G_vmlCanvasManager.init_(document);
};

patchFn.defer(10);
}
