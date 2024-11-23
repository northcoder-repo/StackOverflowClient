$(document).ready(function () {
    $('#example').DataTable({
        ajax: '/data/questions_list.json',
        lengthMenu: [ 10, 25, 50, 100 ],
        responsive: { details: false },
        order: [[ 1, 'asc' ]],
        columns: [
            { data: 'question_id', title: '',
                render: function (data, type, row) {
                    return '<a href="/question/' + row.site + '/'
                            + data + '">view</a>';
                }
            },
            { data: 'title', title: 'Question' },
            { data: 'tags[, ]', title: 'Tags', className: 'desktop tablet-l' },
            { data: 'site', title: 'Site', className: 'desktop' }
        ]
    });
});
