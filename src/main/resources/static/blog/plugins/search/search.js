$(function () {
    $('#searchbox').bind('input propertychange', function () {
        search();
    });
});

var blankQueryFLag = false;

function search() {
    var keyword = $('#searchbox').val();
    if (keyword != '') {
        blankQueryFLag = true;
        var url = '/search/' + keyword;
        $('.articles-list').load(url);
    } else {
        if (blankQueryFLag) {
            var url = '/search/' + keyword;
            $('.articles-list').load(url);
            blankQueryFLag = false;
        }
    }
}