document.getElementById('addBagForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const bag = {
        name: formData.get('name'),
        country: formData.get('country'),
        varietal: formData.get('varietal') ? formData.get('varietal').split(',').map(v => v.trim()) : [],
        process: formData.get('process') ? formData.get('process').split(',').map(p => p.trim()) : [],
        altitude: parseFloat(formData.get('altitude')),
        score: parseFloat(formData.get('score')),
        notes: formData.get('notes') ? formData.get('notes').split(',').map(n => n.trim()) : [],
        roaster: formData.get('roaster'),
        date: new Date().toISOString()
    };

    try {
        const response = await fetch('/bag', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bag)
        });

        if (response.ok) {
            alert('Bag added successfully!');
            window.location.href = '/';
        } else {
            alert('Error adding bag: ' + response.statusText);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
})