package org.review_board.ereviewboard.core.model.reviews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.reviews.core.model.*;
import org.eclipse.mylyn.reviews.internal.core.model.ReviewsFactory;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.TraceLocation;
import org.review_board.ereviewboard.core.client.DiffCommentLineMapper;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.DiffComment;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.User;

/**
 * Bridges between the <tt>eReviewBoard</tt> and the <tt>Mylyn Reviews</tt> model
 * 
 * @author Robert Munteanu
 *
 */
@SuppressWarnings("restriction")
public class ReviewModelFactory {
    
    private static final ReviewsFactory FACTORY = ReviewsFactory.eINSTANCE;
    private final ReviewboardClient _client;
    
    public ReviewModelFactory(ReviewboardClient client) {
        
        _client = client;
    }

    public List<IFileItem> createFileItems(String submitter, ReviewboardDiffMapper diffMapper) {
       
        List<FileDiff> fileDiffs = diffMapper.getFileDiffs();
        
        List<IFileItem> fileItems = new ArrayList<IFileItem>(fileDiffs.size());
        
        for ( FileDiff fileDiff : fileDiffs ) {
            
            IFileItem fileItem = FACTORY.createFileItem();
            fileItem.setAddedBy(createUser(submitter));
            fileItem.setName(fileDiff.getDestinationFile());
            fileItem.setId(String.valueOf(fileDiff.getId()));
            
            IFileRevision from = FACTORY.createFileRevision();
            from.setPath(fileDiff.getSourceFile());
            from.setRevision(fileDiff.getSourceRevision());
            
            IFileRevision to = FACTORY.createFileRevision();
            to.setPath(fileDiff.getDestinationFile());
            to.setRevision(fileDiff.getDestinationDetail());
            
            fileItem.setBase(from);
            fileItem.setTarget(to);
            
            fileItems.add(fileItem);
        }
        
        return fileItems;
    }
    
    public void appendComments(IFileItem fileItem, List<DiffComment> diffComments, DiffCommentLineMapper diffCommentLineMapper) {
        
        List<DiffComment> sortedDiffComments = new ArrayList<DiffComment>(diffComments);
        
        Map<Range, ITopic> rangeToTopics = new HashMap<Range, ITopic>();
        
        for ( DiffComment diffComment : sortedDiffComments ) {
            
            int[] lineMappings = diffCommentLineMapper.getLineMappings(diffComment.getFirstLine());
            
            // prefer 'to' as a target
            IFileRevision fileRevision;
            int mappedLine;
            
            if ( lineMappings[1] != - 1) { 
                fileRevision = fileItem.getTarget();
                mappedLine = lineMappings[1];
            }
            else {
                fileRevision = fileItem.getBase();
                mappedLine = lineMappings[0];
            }
            
            ILineRange line = FACTORY.createLineRange();
            line.setStart(mappedLine);
            line.setEnd(line.getStart() + diffComment.getNumLines());
            
            ReviewboardCorePlugin.getDefault().trace(TraceLocation.MODEL, "Converted " +
            		"DiffComment [" + diffComment.getFirstLine()+ ", " + diffComment.getNumLines()+"] " +
    				"to ILineRange [" + line.getStart()+", " + line.getEnd()+"] on " + fileRevision.getPath() + " ( " + fileRevision.getRevision() + " )");

            ILineLocation location = FACTORY.createLineLocation();
            location.getRanges().add(line);
            
            ITopic topic = rangeToTopics.get(new Range(line));

            IUser author = createUser(diffComment.getUsername());
            if ( topic == null ) {
                topic = createTopic(diffComment, fileRevision, location, author);
                rangeToTopics.put(new Range(line), topic);
            }
            
            IComment topicComment = FACTORY.createComment();
            topicComment.setAuthor(author);
            topicComment.setCreationDate(diffComment.getTimestamp());
            topicComment.setDescription(diffComment.getText());

            
            topic.getComments().add(topicComment);
        }
    }

    private ITopic createTopic(DiffComment diffComment, IFileRevision fileRevision, ILineLocation location, IUser author) {
        
        ITopic topic = FACTORY.createTopic();
        topic.setId(String.valueOf(diffComment.getId()));
        topic.setAuthor(author);
        topic.setCreationDate(diffComment.getTimestamp());
        topic.setLocation(location);
        topic.setItem(fileRevision);
        topic.setDraft(false);
        topic.setDescription(diffComment.getText());
        return topic;
    }

    public IUser createUser(String username) {
        IUser author = FACTORY.createUser();
        
        User user = _client.getClientData().getUser(username);
        author.setId(username);
        if ( user != null && user.getFullName().length() > 0 )
            author.setDisplayName(user.getFullName());
        else
            author.setDisplayName(username);
        return author;
    }

    private static class Range {
        
        private int _start;
        private int _end;
        
        public Range(ILineRange range) {
            _start = range.getStart();
            _end = range.getEnd();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + _end;
            result = prime * result + _start;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Range other = (Range) obj;
            if (_end != other._end)
                return false;
            if (_start != other._start)
                return false;
            return true;
        }
        
        
    }
}
