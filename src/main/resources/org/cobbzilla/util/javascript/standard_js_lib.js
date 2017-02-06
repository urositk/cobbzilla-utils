// returns true if item is in arr array
function found (item, arr) { return arr != null && arr.indexOf(''+item) != -1; }

// returns true if any element in items array is found in arr array
function found_any (items, arr) {
    if (typeof items == 'undefined' || typeof arr == 'undefined' || items == null || arr == null) return false;
    for (var i=0; i<items.length; i++) if (found(items[i], arr)) return true;
    return false;
}

// calculate a percent value, where percentage is between 0 and 100, and amount is some number
function pct (percentage, amount) { return (parseFloat(percentage) / 100.0) * parseFloat(amount); }

// standard comparison functions
function gt (x, compare) { return x > compare; }
function ge (x, compare) { return x >= compare; }
function lt (x, compare) { return x < compare; }
function le (x, compare) { return x <= compare; }
function eq (x, compare) { return x == compare; }
function ne (x, compare) { return x != compare; }

// function to find the first object in array that matches field==value
// field may contain embedded dots to navigate within each object element of the array
function find (arr, field, value, comparison) {
    if (typeof comparison == 'undefined') comparison = eq;
    return arr == null ? null : arr.find(function (obj) {
        if (typeof obj == 'undefined' || obj == null) return false;
        var target = obj;
        var path = field;
        var dotPos = path.indexOf('.');
        while (dotPos != -1) {
            var prop = path.substring(0, dotPos);
            if (!target[prop]) return false;
            target = target[prop];
            path = path.substring(dotPos+1);
            dotPos = path.indexOf('.');
        }
        return target[path] && comparison(target[path], value);
    });
}

function contains (arr, field, comparison, value) {
    var found = find(arr, field, value, comparison);
    return found !== null && found !== false;
}

// function to find the all object in array that match field==value
// field may contain embedded dots to navigate within each object element of the array
function find_all (arr, field, value) {
    var found = [];
    if (arr == null || arr.length == 0) return found;
    arr.find(function (obj) {
        if (typeof obj == 'undefined' || obj == null) return false;
        var target = obj;
        var path = field;
        var dotPos = path.indexOf('.');
        while (dotPos != -1) {
            var prop = path.substring(0, dotPos);
            if (!target[prop]) return false;
            target = target[prop];
            path = path.substring(dotPos+1);
            dotPos = path.indexOf('.');
        }
        if (target[path] && target[path] == value) {
            found.push(obj);
        }
    });
    return found;
}

// returns a function that:
// 1) applies itemFunc function to an item, 2) uses comparison function to compare the result against compareVal
function compare (itemFunc, comparison, compareVal) {
    return function (item) {
        if (typeof item != 'undefined') {
            var val = itemFunc(item);
            if (typeof val != 'undefined') {
                return comparison(itemFunc(item), compareVal);
            }
        }
        return false;
    };
}

// return an itemFunc that treats item.field as a percentage, and multiplies it by total, and compares it against compareVal
function compare_pct (field, total, comparison, compareVal) {
    return function (item) { return comparison(pct(item[field], total), compareVal); }
}

// apply itemFunc to each item in array arr. if any such invocation of itemFunc returns true, then this function returns true
function match_any (arr, itemFunc) {
    return arr != null && arr.length > 0 && arr.find(itemFunc) != 'undefined';
}

// functions for rounding up/down to nearest multiple
function up (x, multiple) { return multiple * parseInt(Math.ceil(parseFloat(x)/parseFloat(multiple))); }
function down (x, multiple) { return multiple * parseInt(Math.floor(parseFloat(x)/parseFloat(multiple))); }

