$(document).ready(function () {
    $('#example').DataTable({
        ajax: '/data/questions_list.json',
        lengthMenu: [10, 25, 50, 100],
        columns: [
            {data: 'question_id', title: 'ID',
                render: function (data, type, row) {
                    return '<a href="/question/' + row.site + '/'
                            + data + '">' + data + '</a>';
                }
            },
            {data: 'title', title: 'Question'},
            {data: 'tags[, ]', title: 'Tags'},
            {data: 'site', title: 'Site'}
        ]
    });
});
