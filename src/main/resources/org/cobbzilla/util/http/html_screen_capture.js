// inspired by https://github.com/pofider/phantom-html-to-pdf/pull/16#issuecomment-155020653

var page = require('webpage').create();
var fs = require('fs');

var url = '@@URL@@';
var outFile = '@@FILE@@';

function checkExists (path, start, timeout) {
    if (fs.exists(path)) return true;
    if (Date.now() - start > timeout) {
        console.log('checkExists: timeout waiting for '+path);
        phantom.exit(2);
    }
    window.setTimeout(function () {
        if (checkExists(path, start, timeout)) {
            phantom.exit(0);
        }
    }, 250);
}
page.paperSize = { width: '595px', height: '842px', margin: '0px' };
page.zoomFactor = 1.25;

page.open(url, function(status) {
  if (status !== 'success') {
    console.log('Unable to load: '+url);
    phantom.exit(1);

  } else {
      //page.evaluate(function () {
          page.render(outFile);
          //checkExists(outFile, Date.now(), 60000);
          //phantom.exit(0);
      //});
  }
});
