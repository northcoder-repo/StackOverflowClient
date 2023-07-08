// helper function - gets an element by its ID:
let get_ele = id => document.getElementById(id);
 
// attached to loader button:
document.addEventListener("DOMContentLoaded", function (event) {
    const el = get_ele("do_load");
    el.addEventListener("click", submitList, false);
});

// Uses a websocket to submit a list of question IDs to the server.
// Displays progress info as this list is processed.
function submitList() {
    const el = get_ele("do_load");
    el.removeEventListener("click", submitList, false);
    
    get_ele("loader_messages").innerHTML = "Starting...";

    let ws = new WebSocket("ws://localhost:8091/websocket/do_load");

    ws.onopen = () => {
        let site_select = get_ele("site-select");
        let site = site_select.options[site_select.selectedIndex].value;
        let id_list = get_ele("id_list").value;
        ws.send(buildIdListJson(site, id_list));
    };

    ws.onmessage = (event) => {
        let div = document.createElement('div');
        div.innerHTML = event.data;
        let container = get_ele("loader_messages");
        container.append(div);
        container.scrollTop = container.scrollHeight;
    };

    ws.onclose = () => {
        el.addEventListener("click", submitList, false);
    };
}

function buildIdListJson(site, id_list) {
    // split by whitespace, convert array elements to numbers, remove
    // any element which are not numbers: 
    id_array = id_list
            .split(/\s+/)
            .map(Number)
            .filter(x => !isNaN(x));
    // remove duplicates:
    ids_deduped = [ ...new Set(id_array) ];
    return JSON.stringify({ "question_site": site, "question_ids": ids_deduped }); 
}