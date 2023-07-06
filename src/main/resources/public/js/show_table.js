$(document).ready(function () {
    $('#example').DataTable({
        ajax: '/questions_list',
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
